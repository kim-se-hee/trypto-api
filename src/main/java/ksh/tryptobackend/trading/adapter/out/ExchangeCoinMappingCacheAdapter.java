package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.trading.application.port.out.ExchangeCoinMappingCachePort;
import ksh.tryptobackend.trading.domain.vo.ExchangeSymbolKey;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExchangeCoinMappingCacheAdapter implements ExchangeCoinMappingCachePort {

    private final ConcurrentHashMap<ExchangeSymbolKey, Long> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<Long> resolve(String exchange, String symbol) {
        return Optional.ofNullable(cache.get(new ExchangeSymbolKey(exchange, symbol)));
    }

    @Override
    public void loadAll(Map<ExchangeSymbolKey, Long> mappings) {
        cache.clear();
        cache.putAll(mappings);
    }
}
