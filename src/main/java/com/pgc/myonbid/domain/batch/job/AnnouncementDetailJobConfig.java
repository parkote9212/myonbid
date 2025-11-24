package com.pgc.myonbid.domain.batch.job;

import com.pgc.myonbid.domain.announcement.Announcement;
import com.pgc.myonbid.domain.announcement.AnnouncementRepository;
import com.pgc.myonbid.domain.batch.OnbidApiService;
import com.pgc.myonbid.domain.batch.dto.OnbidAnnouncementDetailResponse;
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

import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AnnouncementDetailJobConfig {

    private final OnbidApiService onbidApiService;
    private final AnnouncementRepository announcementRepository;
    private final AuctionHistoryRepository historyRepository;

    private static final int CHUNK_SIZE = 50; // HTML 텍스트라 용량이 클 수 있으니 조금씩 처리

    @Bean
    public Job announcementDetailJob(JobRepository jobRepository, Step announcementDetailStep) {
        return new JobBuilder("announcementDetailJob", jobRepository)
                .start(announcementDetailStep)
                .build();
    }

    @Bean
    public Step announcementDetailStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("announcementDetailStep", jobRepository)
                .tasklet(announcementDetailTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet announcementDetailTasklet() {
        return (contribution, chunkContext) -> {
            // 1. 업데이트 대상 조회
            List<Announcement> targets = announcementRepository.findIncompleteAnnouncements(PageRequest.of(0, CHUNK_SIZE));

            if (targets.isEmpty()) {
                log.info("업데이트할 공고 상세 데이터가 없습니다. (작업 종료)");
                return RepeatStatus.FINISHED;
            }

            log.info(">>> 공고 상세 업데이트 시작 (대상: {}건)", targets.size());

            int successCount = 0;
            for (Announcement announcement : targets) {
                try {
                    updateAnnouncementDetails(announcement);
                    successCount++;
                    Thread.sleep(100); // API 보호
                } catch (Exception e) {
                    log.error("공고 업데이트 실패 (PLNM_NO: {}): {}", announcement.getPlnmNo(), e.getMessage());
                }
            }

            log.info(">>> {}건 업데이트 완료.", successCount);
            return RepeatStatus.CONTINUABLE;
        };
    }

    @Transactional
    protected void updateAnnouncementDetails(Announcement announcement) {
        // API 호출을 위해 해당 공고에 연결된 아무 PBCT_NO나 하나 가져옴 (자식 테이블 조회)
        // AuctionHistoryRepository에 findFirstByAnnouncement_PlnmNo 메서드가 필요할 수 있음.
        // 여기서는 findAllBy... 로 가져와서 하나 뽑음 (없으면 패스)
        // ※ 성능을 위해 Repository에 findFirstByAnnouncementPlnmNo(String plnmNo)를 추가하는 게 좋음.

        // 임시로 전체 조회 후 stream (데이터 많으면 비효율적이나 현재 단계에선 OK)
        // 더 좋은 방법: AuctionHistoryRepository에 메서드 추가
        // Optional<AuctionHistory> historyOpt = historyRepository.findFirstByPlnmNo(announcement.getPlnmNo());

        // 아래는 Repository 수정 없이 JPA 메서드 쿼리 사용 예시 (historyRepository에 메서드 추가 필요!)
        Optional<AuctionHistory> historyOpt = historyRepository.findFirstByAnnouncement(announcement);

        if (historyOpt.isEmpty()) {
            // 자식(이력)이 없으면 상세 조회를 못함 -> 일단 건너뜀 (나중에 수집될 수도 있음)
            return;
        }

        String pbctNo = historyOpt.get().getPbctNo();
        OnbidAnnouncementDetailResponse response = onbidApiService.getAnnouncementDetail(announcement.getPlnmNo(), pbctNo);

        if (response != null && response.getBody() != null && response.getBody().getItem() != null) {
            OnbidAnnouncementDetailResponse.DetailItem item = response.getBody().getItem();

            // 엔티티 업데이트 (Dirty Checking)
            // Announcement 엔티티에 updateDetails 메서드가 있어야 함
            announcement.updateDetails(
                    item.getPlnmNm(),
                    item.getOrgNm(),
                    item.getRsbyDept(),
                    item.getPscgNm(),
                    item.getPscgTpno(),
                    item.getPlnmDoc() // HTML
            );
        }
    }
}