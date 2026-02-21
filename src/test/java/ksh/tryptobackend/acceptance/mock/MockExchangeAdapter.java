package ksh.tryptobackend.acceptance.mock;

import ksh.tryptobackend.trading.application.port.out.ExchangePort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockExchangeAdapter implements ExchangePort {

    private final Map<Long, ExchangeData> exchanges = new ConcurrentHashMap<>();

    @Override
    public Optional<ExchangeData> findById(Long exchangeId) {
        return Optional.ofNullable(exchanges.get(exchangeId));
    }

    public void addExchange(ExchangeData data) {
        exchanges.put(data.exchangeId(), data);
    }

    public void clear() {
        exchanges.clear();
    }
}
