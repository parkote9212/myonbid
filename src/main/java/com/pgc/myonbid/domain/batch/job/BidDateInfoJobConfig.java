package com.pgc.myonbid.domain.batch.job;

import com.pgc.myonbid.domain.batch.OnbidApiService;
import com.pgc.myonbid.domain.batch.dto.OnbidBidDateInfoResponse;
import com.pgc.myonbid.domain.history.AuctionHistory;
import com.pgc.myonbid.domain.history.AuctionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BidDateInfoJobConfig {

    private final OnbidApiService onbidApiService;
    private final AuctionHistoryRepository historyRepository;

    // 한 번에 처리할 데이터 수 (API 호출 횟수 고려해서 적절히 조절)
    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job bidDateInfoJob(JobRepository jobRepository, Step bidDateInfoStep) {
        return new JobBuilder("bidDateInfoJob", jobRepository)
                .start(bidDateInfoStep)
                .build();
    }

    @Bean
    public Step bidDateInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("bidDateInfoStep", jobRepository)
                .tasklet(bidDateInfoTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet bidDateInfoTasklet() {
        return (contribution, chunkContext) -> {
            // 1. 업데이트가 필요한 데이터 조회 (SEQ가 NULL인 것)
            List<AuctionHistory> targets = historyRepository.findIncompleteHistories(PageRequest.of(0, CHUNK_SIZE));

            if (targets.isEmpty()) {
                log.info("업데이트할 상세 데이터가 없습니다. (작업 종료)");
                return RepeatStatus.FINISHED;
            }

            log.info(">>> 상세 정보 업데이트 시작 (대상: {}건)", targets.size());

            int successCount = 0;
            for (AuctionHistory history : targets) {
                try {
                    updateHistoryDetails(history);
                    successCount++;
                    // API 과부하 방지 (0.05초 대기)
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("상세 업데이트 실패 (PBCT_NO: {}): {}", history.getPbctNo(), e.getMessage());
                }
            }

            log.info(">>> {}건 업데이트 완료. 계속 진행합니다...", successCount);

            // 데이터가 남아있으면 계속 실행 (Loop)
            return RepeatStatus.CONTINUABLE;
        };
    }

    @Transactional
    protected void updateHistoryDetails(AuctionHistory history) {
        // API 호출
        OnbidBidDateInfoResponse response = onbidApiService.getBidDateInfo(history.getAnnouncement().getPlnmNo(), history.getPbctNo());

        if (response != null && response.getBody().getItems().getDateList() != null) {
            // 리스트 중 내 공매번호와 일치하는 것 찾기 (보통 1개옴)
            response.getBody().getItems().getDateList().stream()
                    .filter(dto -> dto.getPbctNo().equals(history.getPbctNo()))
                    .findFirst()
                    .ifPresent(dto -> {
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

                        // Entity 업데이트 (Dirty Checking으로 자동 저장됨)
                        history.updateSchedule(
                                parseDate(dto.getPbctBegnDtm(), fmt),
                                parseDate(dto.getPbctClsDtm(), fmt),
                                parseDate(dto.getPbctExctDtm(), fmt),
                                dto.getTdpsRt()
                        );
                        // 추가로 필요한 필드(회차, 차수)도 여기서 세팅
                        // history.setPbctSeq(dto.getPbctSeq()); (Entity에 Setter나 update 메서드 필요)
                    });
        }
    }

    private LocalDateTime parseDate(String dateStr, DateTimeFormatter fmt) {
        try {
            return (dateStr != null && !dateStr.isEmpty()) ? LocalDateTime.parse(dateStr, fmt) : null;
        } catch (Exception e) { return null; }
    }
}