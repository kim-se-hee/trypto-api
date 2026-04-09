package ksh.tryptobackend.trading.adapter.in;

import ksh.tryptobackend.trading.application.port.in.WarmupPendingOrderMatchingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingOrderMatchingWarmupInitializer {

    private final WarmupPendingOrderMatchingUseCase warmupPendingOrderMatchingUseCase;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("미체결 주문 매칭 워밍업 시작");

        warmupPendingOrderMatchingUseCase.warmup();

        log.info("미체결 주문 매칭 워밍업 완료");
    }
}
