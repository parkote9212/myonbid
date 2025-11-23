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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional; // ★ 중요

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CltrListJobConfig {

    private final OnbidApiService onbidApiService;
    private final ItemRepository itemRepository;
    private final AnnouncementRepository announcementRepository;
    private final AuctionHistoryRepository historyRepository;

    private static final int PAGE_SIZE = 100;

    @Bean
    public Job cltrListJob(JobRepository jobRepository, Step cltrListStep) {
        return new JobBuilder("cltrListJob", jobRepository)
                .start(cltrListStep)
                .build();
    }

    // ★ [핵심 수정 포인트] transactionManager를 파라미터로 받고 tasklet에 넘겨줘야 함
    @Bean
    public Step cltrListStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("cltrListStep", jobRepository)
                .tasklet(cltrListTasklet(), transactionManager) // <--- 여기가 없으면 DB 저장이 안 됩니다!
                .build();
    }

    @Bean
    public Tasklet cltrListTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> 공매 물건 목록 수집 시작");

            int pageNo = 1;
            boolean hasNext = true;

            while (hasNext) {
                log.info("Fetching page: {}", pageNo);

                // 1. API 호출
                OnbidCltrListResponse response = onbidApiService.getCltrList(pageNo, PAGE_SIZE);

                if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
                    log.warn("응답 데이터 없음. 수집 종료.");
                    break;
                }

                // ★ 만약 items 안에 item 리스트가 없다면 null 처리 필요
                if (response.getBody().getItems().getItemList() == null) {
                    log.warn("페이지 {}에 아이템 목록이 비어있습니다.", pageNo);
                    break;
                }

                List<OnbidCltrListResponse.ItemDto> items = response.getBody().getItems().getItemList();
                if (items.isEmpty()) {
                    log.info("더 이상 데이터가 없습니다. (Page: {})", pageNo);
                    break;
                }

                // 2. 데이터 저장
                for (OnbidCltrListResponse.ItemDto dto : items) {
                    try {
                        saveOrUpdateData(dto);
                    } catch (Exception e) {
                        log.error("데이터 저장 실패 (물건번호: {}): {}", dto.getCltrNo(), e.getMessage());
                    }
                }

                // 3. 다음 페이지 로직
                if (items.size() < PAGE_SIZE) {
                    hasNext = false;
                } else {
                    pageNo++;
                    Thread.sleep(100);
                }
            }

            log.info(">>>>> 공매 물건 목록 수집 완료");
            return RepeatStatus.FINISHED;
        };
    }

    // [트랜잭션 처리] 이 메서드가 실행될 때마다 Commit이 일어나도록 설정
    // (대량 데이터일 땐 성능 이슈가 있지만, 지금은 확실한 저장을 위해 사용)
    @Transactional
    protected void saveOrUpdateData(OnbidCltrListResponse.ItemDto dto) {

        // A. 물건(Item) 저장
        Item item = itemRepository.findById(dto.getCltrNo())
                .orElseGet(() -> itemRepository.save(dto.toItemEntity()));

        // B. 공고(Announcement) 저장
        Announcement announcement = null;
        if (dto.getPlnmNo() != null) {
            announcement = announcementRepository.findById(dto.getPlnmNo())
                    .orElseGet(() -> announcementRepository.save(
                            Announcement.builder()
                                    .plnmNo(dto.getPlnmNo())
                                    .plnmNm("임시저장_상세업데이트필요")
                                    .build()
                    ));
        }

        // C. 공매 이력(History) 저장
        if (!historyRepository.existsByPbctNo(dto.getPbctNo())) {
            if (announcement != null) {
                AuctionHistory history = dto.toHistoryEntity(item, announcement);
                historyRepository.save(history);
            }
        }
    }
}