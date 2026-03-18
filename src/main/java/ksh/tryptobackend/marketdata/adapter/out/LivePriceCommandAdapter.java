package ksh.tryptobackend.marketdata.adapter.out;

import ksh.tryptobackend.marketdata.application.port.out.LivePriceCommandPort;
import ksh.tryptobackend.marketdata.domain.vo.LiveTicker;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LivePriceCommandAdapter implements LivePriceCommandPort {

    private static final String TOPIC_PREFIX = "/topic/prices.";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void send(Long exchangeId, LiveTicker liveTicker) {
        messagingTemplate.convertAndSend(TOPIC_PREFIX + exchangeId, liveTicker);
    }
}
