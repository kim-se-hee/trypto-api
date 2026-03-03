package ksh.tryptobackend.common.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final JobOperator jobOperator;
    private final Job snapshotJob;
    private final Job regretReportJob;
    private final Job rankingJob;
    private final TaskExecutor batchTaskExecutor;

    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Seoul")
    public void runDailyBatch() {
        LocalDate snapshotDate = LocalDate.now(KST);
        JobParameters params = new JobParametersBuilder()
            .addString("snapshotDate", snapshotDate.toString())
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        try {
            log.info("배치 시작: snapshotDate={}", snapshotDate);

            jobOperator.start(snapshotJob, params);
            log.info("SnapshotJob 완료");

            CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> runJob(regretReportJob, params), batchTaskExecutor),
                CompletableFuture.runAsync(() -> runJob(rankingJob, params), batchTaskExecutor)
            ).join();

            log.info("배치 완료: snapshotDate={}", snapshotDate);
        } catch (Exception e) {
            log.error("배치 실패: snapshotDate={}", snapshotDate, e);
        }
    }

    private void runJob(Job job, JobParameters params) {
        try {
            jobOperator.start(job, params);
            log.info("{} 완료", job.getName());
        } catch (Exception e) {
            log.error("{} 실패", job.getName(), e);
        }
    }
}
