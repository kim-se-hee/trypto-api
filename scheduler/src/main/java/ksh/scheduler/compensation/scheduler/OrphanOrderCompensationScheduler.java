package ksh.scheduler.compensation.scheduler;

import ksh.scheduler.compensation.detector.MissedFillDetector;
import ksh.scheduler.compensation.model.PendingOrder;
import ksh.scheduler.compensation.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanOrderCompensationScheduler {

    private final OrderJpaRepository orderJpaRepository;
    private final MissedFillDetector detector;

    @Value("${compensation.boundary-seconds}")
    private int boundarySeconds;

    @Scheduled(fixedDelayString = "${compensation.interval-ms}")
    @SchedulerLock(name = "orphan-order-compensation",
                   lockAtMostFor = "PT10M",
                   lockAtLeastFor = "PT30S")
    public void scan() {
        List<PendingOrder> pending = orderJpaRepository.findBoundaryPending(boundarySeconds);
        if (pending.isEmpty()) return;
        log.info("compensation scan pending={}", pending.size());
        int filled = 0;
        for (PendingOrder o : pending) {
            if (detector.tryCompensate(o)) filled++;
        }
        if (filled > 0) log.info("compensation filled {} of {}", filled, pending.size());
    }
}
