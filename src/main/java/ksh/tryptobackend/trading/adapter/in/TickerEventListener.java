package ksh.tryptobackend.trading.adapter.in;

import ksh.tryptobackend.trading.adapter.in.dto.TickerMessage;
import ksh.tryptobackend.trading.application.service.MatchPendingOrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TickerEventListener {

    private final MatchPendingOrdersService matchPendingOrdersService;

    @RabbitListener(queues = "#{tickerMatchingQueue.name}", autoStartup = "false", id = "tickerMatchingListener")
    public void onTickerEvent(TickerMessage message) {
        try {
            matchPendingOrdersService.matchOrders(
                message.exchange(), message.symbol(), message.currentPrice());
        } catch (Exception e) {
            log.error("시세 이벤트 처리 실패: exchange={}, symbol={}",
                message.exchange(), message.symbol(), e);
        }
    }
}
