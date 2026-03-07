package ksh.tryptobackend.marketdata.adapter.out;

import ksh.tryptobackend.marketdata.adapter.out.entity.CoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.repository.CoinJpaRepository;
import ksh.tryptobackend.marketdata.application.port.out.CoinQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CoinQueryAdapter implements CoinQueryPort {

    private final CoinJpaRepository coinJpaRepository;

    @Override
    public Map<Long, String> findSymbolsByIds(Set<Long> coinIds) {
        if (coinIds.isEmpty()) {
            return Map.of();
        }
        return coinJpaRepository.findByIdIn(coinIds).stream()
            .collect(Collectors.toMap(CoinJpaEntity::getId, CoinJpaEntity::getSymbol));
    }

}
