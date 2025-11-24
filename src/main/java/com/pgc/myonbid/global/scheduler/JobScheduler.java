package com.pgc.myonbid.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling // 스케줄러 활성화
@RequiredArgsConstructor
public class JobScheduler {

    private final JobLauncher jobLauncher;
    private final Job cltrListJob; // 위에서 만든 Job Bean 주입
    private final Job bidDateInfoJob;
    private final Job announcementDetailJob;
    private final Job fileInfoJob;
    // 매일 새벽 2시에 실행 (Cron 표현식)
    // 테스트할 땐: @Scheduled(initialDelay = 5000, fixedDelay = 1000 * 60 * 60) // 시작 5초 후 실행, 이후 1시간마다
//    @Scheduled(cron = "0 0 2 * * *")
    @Scheduled(initialDelay = 5000, fixedDelay = 1000 * 60 * 60)
    public void runJob() {
        try {
            log.info(">>> 스케줄러에 의해 Batch Job이 시작됩니다.");

            // 배치는 실행될 때마다 고유한 파라미터(시간)가 필요함 (중복 실행 방지)
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

//            jobLauncher.run(cltrListJob, jobParameters);
//            jobLauncher.run(bidDateInfoJob, jobParameters);
//            jobLauncher.run(announcementDetailJob, jobParameters);
            jobLauncher.run(fileInfoJob, jobParameters);

        } catch (Exception e) {
            log.error("배치 실행 중 에러 발생: {}", e.getMessage());
        }
    }
}