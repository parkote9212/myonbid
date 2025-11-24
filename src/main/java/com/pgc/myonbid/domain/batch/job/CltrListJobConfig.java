package com.pgc.myonbid.domain.batch.job;

import com.pgc.myonbid.domain.announcement.Announcement;
import com.pgc.myonbid.domain.announcement.AnnouncementRepository;
import com.pgc.myonbid.domain.batch.OnbidApiService;
import com.pgc.myonbid.domain.batch.dto.OnbidCltrListResponse;
import com.pgc.myonbid.domain.history.AuctionHistory;
import com.pgc.myonbid.domain.history.AuctionHistoryRepository;
import com.pgc.myonbid.domain.item.Item;
import com.pgc.myonbid.domain.item.ItemRepository;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CltrListJobConfig {

    private final OnbidApiService onbidApiService;
    private final ItemRepository itemRepository;
    private final AnnouncementRepository announcementRepository;
    private final AuctionHistoryRepository historyRepository;

    private static final int PAGE_SIZE = 1000;
    private static final String PAGE_KEY = "currentPage"; // ExecutionContext에 저장할 키

    @Bean
    public Job cltrListJob(JobRepository jobRepository, Step cltrListStep) {
        return new JobBuilder("cltrListJob", jobRepository)
                .start(cltrListStep)
                .build();
    }

    @Bean
    public Step cltrListStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("cltrListStep", jobRepository)
                .tasklet(cltrListTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet cltrListTasklet() {
        return (contribution, chunkContext) -> {
            // 1. 현재 페이지 번호를 ExecutionContext에서 가져옴 (없으면 1부터 시작)
            ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
            int pageNo = executionContext.getInt(PAGE_KEY, 1);

            log.info("Fetching page: {}", pageNo);

            // 2. API 호출
            OnbidCltrListResponse response = onbidApiService.getCltrList(pageNo, PAGE_SIZE);

            // 데이터 검증
            if (response == null || response.getBody() == null ||
                    response.getBody().getItems() == null ||
                    response.getBody().getItems().getItemList() == null) {
                log.warn("응답 데이터 없음. 수집 종료.");
                return RepeatStatus.FINISHED; // 종료
            }

            List<OnbidCltrListResponse.ItemDto> items = response.getBody().getItems().getItemList();
            if (items.isEmpty()) {
                log.info("더 이상 데이터가 없습니다. (Page: {}) - 수집 종료", pageNo);
                return RepeatStatus.FINISHED; // 종료
            }

            // 3. 데이터 저장 (100건)
            for (OnbidCltrListResponse.ItemDto dto : items) {
                try {
                    saveOrUpdateData(dto);
                } catch (Exception e) {
                    log.error("데이터 저장 실패 (물건번호: {}): {}", dto.getCltrNo(), e.getMessage());
                }
            }

            // 4. 다음 페이지 준비
            executionContext.putInt(PAGE_KEY, pageNo + 1); // 다음 페이지 번호 저장

            // API 과부하 방지 (0.1초 대기)
            Thread.sleep(100);

            // 5. ★ 핵심: 데이터가 있으면 'CONTINUABLE'을 리턴해서 다시 실행하게 함 (Loop 효과)
            if (items.size() < PAGE_SIZE) {
                return RepeatStatus.FINISHED; // 마지막 페이지면 종료
            } else {
                return RepeatStatus.CONTINUABLE; // 계속해라! (이때 커밋됨)
            }
        };
    }

    @Transactional
    protected void saveOrUpdateData(OnbidCltrListResponse.ItemDto dto) {
        // 기존 로직 동일
        Item item = itemRepository.findById(dto.getCltrNo())
                .orElseGet(() -> itemRepository.saveAndFlush(dto.toItemEntity()));

        Announcement announcement = null;
        if (dto.getPlnmNo() != null) {
            announcement = announcementRepository.findById(dto.getPlnmNo())
                    .orElseGet(() -> announcementRepository.saveAndFlush(
                            Announcement.builder()
                                    .plnmNo(dto.getPlnmNo())
                                    .plnmNm("임시저장_상세업데이트필요")
                                    .build()
                    ));
        }

        if (!historyRepository.existsByPbctNo(dto.getPbctNo())) {
            if (announcement != null) {
                AuctionHistory history = dto.toHistoryEntity(item, announcement);
                historyRepository.save(history);
            }
        }
    }
}