package ksh.tryptobackend.batch.regretreport;

import ksh.tryptobackend.regretanalysis.domain.model.RegretReport;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RegretReportJobConfig {

    private static final int CHUNK_SIZE = 10;
    private static final int RETRY_LIMIT = 5;
    private static final long INITIAL_INTERVAL = 200L;
    private static final double MULTIPLIER = 2.0;
    private static final long MAX_INTERVAL = 5000L;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job regretReportJob(Step regretReportStep) {
        return new JobBuilder("regret-report-job", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(regretReportStep)
            .build();
    }

    @Bean
    public Step regretReportStep(RegretReportItemReader reader,
                                 RegretReportItemProcessor processor,
                                 RegretReportItemWriter writer) {
        return new StepBuilder("regret-report-step", jobRepository)
            .<RegretReportInput, RegretReport>chunk(CHUNK_SIZE)
            .transactionManager(transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .retryPolicy(regretReportRetryPolicy())
            .build();
    }

    private RetryPolicy regretReportRetryPolicy() {
        return RetryPolicy.builder()
            .maxRetries(RETRY_LIMIT)
            .delay(Duration.ofMillis(INITIAL_INTERVAL))
            .multiplier(MULTIPLIER)
            .maxDelay(Duration.ofMillis(MAX_INTERVAL))
            .includes(TransientDataAccessException.class)
            .build();
    }
}
