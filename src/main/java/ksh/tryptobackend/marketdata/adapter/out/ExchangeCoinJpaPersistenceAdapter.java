package ksh.tryptobackend.marketdata.adapter.out;

import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeCoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.repository.ExchangeCoinJpaRepository;
import ksh.tryptobackend.marketdata.application.port.out.ExchangeCoinQueryPort;
import ksh.tryptobackend.marketdata.application.port.out.dto.ExchangeCoinMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExchangeCoinJpaPersistenceAdapter implements ExchangeCoinQueryPort {

    private final ExchangeCoinJpaRepository repository;

    @Override
    public Optional<ExchangeCoinMapping> findById(Long exchangeCoinId) {
        return repository.findById(exchangeCoinId)
            .map(e -> new ExchangeCoinMapping(e.getId(), e.getExchangeId(), e.getCoinId()));
    }

    @Override
    public Optional<Long> findExchangeCoinId(Long exchangeId, Long coinId) {
        return repository.findByExchangeIdAndCoinId(exchangeId, coinId)
            .map(ExchangeCoinJpaEntity::getId);
    }

    @Override
    public Map<Long, Long> findExchangeCoinIdMap(Long exchangeId, List<Long> coinIds) {
        return repository.findByExchangeIdAndCoinIdIn(exchangeId, coinIds).stream()
            .collect(Collectors.toMap(ExchangeCoinJpaEntity::getCoinId, ExchangeCoinJpaEntity::getId));
    }
}
