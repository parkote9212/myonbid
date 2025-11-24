package com.pgc.myonbid.domain.batch.job;

import com.pgc.myonbid.domain.batch.OnbidApiService;
import com.pgc.myonbid.domain.batch.dto.OnbidFileInfoResponse;
import com.pgc.myonbid.domain.history.AttachmentFile;
import com.pgc.myonbid.domain.history.AttachmentFileRepository;
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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileInfoJobConfig {

    private final OnbidApiService onbidApiService;
    private final AuctionHistoryRepository historyRepository;
    private final AttachmentFileRepository fileRepository;

    private static final int CHUNK_SIZE = 100;
    private static final String PAGE_KEY = "currentFilePage"; // 페이지 번호 저장 키

    @Bean
    public Job fileInfoJob(JobRepository jobRepository, Step fileInfoStep) {
        return new JobBuilder("fileInfoJob", jobRepository)
                .start(fileInfoStep)
                .build();
    }

    @Bean
    public Step fileInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("fileInfoStep", jobRepository)
                .tasklet(fileInfoTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet fileInfoTasklet() {
        return (contribution, chunkContext) -> {
            // 1. 현재 페이지 번호 가져오기 (없으면 0부터 시작)
            ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
            int page = executionContext.getInt(PAGE_KEY, 0);

            log.info(">>> 첨부파일 정보 수집 중... (Page: {}, Size: {})", page, CHUNK_SIZE);

            // 2. DB에서 100건씩 끊어서 조회 (최신순 정렬 권장)
            // 주의: 데이터가 많을 경우 Offset 방식 페이징은 뒤로 갈수록 느려질 수 있으나, 초기 구축용으로는 충분함
            List<AuctionHistory> targets = historyRepository.findAll(
                    PageRequest.of(page, CHUNK_SIZE, Sort.by(Sort.Direction.DESC, "historyId"))
            ).getContent();

            if (targets.isEmpty()) {
                log.info(">>> 모든 데이터 처리 완료. (Total Pages: {})", page);
                return RepeatStatus.FINISHED; // 더 이상 데이터 없으면 종료
            }

            int successCount = 0;
            for (AuctionHistory history : targets) {
                try {
                    saveFileInfos(history);
                    successCount++;
                    // API 호출 간격 (너무 빠르면 차단될 수 있으니 조절)
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("파일 저장 실패 (PBCT_NO: {}): {}", history.getPbctNo(), e.getMessage());
                }
            }

            log.info(">>> 현재 페이지 {}건 처리 완료.", successCount);

            // 3. 다음 페이지 설정 및 반복
            executionContext.putInt(PAGE_KEY, page + 1);
            return RepeatStatus.CONTINUABLE; // 계속 실행!
        };
    }

    @Transactional
    protected void saveFileInfos(AuctionHistory history) {
        // 이미 수집된 파일이 있는지 체크 (중복 호출 방지 최적화)
        // (historyId 등을 이용해 파일 테이블을 조회하는 로직이 있다면 추가)

        OnbidFileInfoResponse response = onbidApiService.getFileInfo(
                history.getAnnouncement().getPlnmNo(),
                history.getPbctNo()
        );
        if (response != null && response.getBody() != null && response.getBody().getItems() != null) {
            log.info("물건({}) 파일 개수: {}", history.getPbctNo(), response.getBody().getItems().getFileList().size());
        } else {
            // log.info("물건({}) 파일 없음", history.getPbctNo()); // 너무 많이 찍힐 수 있으니 주석 처리
        }

        if (response != null && response.getBody() != null
                && response.getBody().getItems() != null
                && response.getBody().getItems().getFileList() != null) {

            List<OnbidFileInfoResponse.FileDto> files = response.getBody().getItems().getFileList();

            for (OnbidFileInfoResponse.FileDto fileDto : files) {
                // 중복 저장 방지 로직 (선택 사항)
                // if (!fileRepository.existsByAtchFilePtcsNo(fileDto.getAtchFilePtcsNo())) { ... }

                AttachmentFile fileEntity = fileDto.toEntity(history.getPbctNo());
                fileRepository.save(fileEntity);
            }
        }
    }
}