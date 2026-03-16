package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.trading.application.port.out.PendingOrderCacheCommandPort;
import org.springframework.stereotype.Component;

@Component
public class PendingOrderCacheCommandAdapter implements PendingOrderCacheCommandPort {

    @Override
    public void remove(Long exchangeCoinId, Long orderId) {
    }
}
