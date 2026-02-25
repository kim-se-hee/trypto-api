package ksh.tryptobackend.acceptance.mock;

import ksh.tryptobackend.trading.application.port.out.ExchangeCoinPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockExchangeCoinAdapter implements ExchangeCoinPort {

    private final Map<Long, ExchangeCoinData> exchangeCoins = new ConcurrentHashMap<>();

    @Override
    public Optional<ExchangeCoinData> findById(Long exchangeCoinId) {
        return Optional.ofNullable(exchangeCoins.get(exchangeCoinId));
    }

    public void addExchangeCoin(ExchangeCoinData data) {
        exchangeCoins.put(data.exchangeCoinId(), data);
    }

    public void clear() {
        exchangeCoins.clear();
    }
}
