package ksh.tryptobackend.marketdata.adapter.out;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeCoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.entity.QCoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.entity.QExchangeCoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.entity.QExchangeJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.repository.ExchangeCoinJpaRepository;
import ksh.tryptobackend.marketdata.application.port.out.ExchangeCoinQueryPort;
import ksh.tryptobackend.marketdata.domain.model.ExchangeCoin;
import ksh.tryptobackend.marketdata.domain.vo.ExchangeCoinIdMap;
import ksh.tryptobackend.marketdata.domain.vo.ExchangeSymbolKey;
import ksh.tryptobackend.marketdata.domain.vo.MarketSymbols;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExchangeCoinQueryAdapter implements ExchangeCoinQueryPort {

    private final ExchangeCoinJpaRepository repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ExchangeCoin> findById(Long exchangeCoinId) {
        return repository.findById(exchangeCoinId)
            .map(ExchangeCoinJpaEntity::toDomain);
    }

    @Override
    public boolean existsByExchangeIdAndCoinId(Long exchangeId, Long coinId) {
        return repository.existsByExchangeIdAndCoinId(exchangeId, coinId);
    }

    @Override
    public ExchangeCoinIdMap findExchangeCoinIdMap(Long exchangeId, List<Long> coinIds) {
        Map<Long, Long> map = repository.findByExchangeIdAndCoinIdIn(exchangeId, coinIds).stream()
            .collect(Collectors.toMap(ExchangeCoinJpaEntity::getCoinId, ExchangeCoinJpaEntity::getId));
        return new ExchangeCoinIdMap(map);
    }

    @Override
    public List<ExchangeCoin> findByExchangeId(Long exchangeId) {
        return repository.findByExchangeId(exchangeId).stream()
            .map(ExchangeCoinJpaEntity::toDomain)
            .toList();
    }

    @Override
    public MarketSymbols findMarketSymbolsByIds(Collection<Long> exchangeCoinIds) {
        if (exchangeCoinIds.isEmpty()) {
            return new MarketSymbols(Collections.emptyMap());
        }

        QExchangeCoinJpaEntity ec = QExchangeCoinJpaEntity.exchangeCoinJpaEntity;
        QExchangeJpaEntity em = QExchangeJpaEntity.exchangeJpaEntity;
        QCoinJpaEntity baseCoin = QCoinJpaEntity.coinJpaEntity;

        List<Tuple> rows = queryFactory
            .select(ec.id, em.name, ec.displayName, baseCoin.symbol)
            .from(ec)
            .join(em).on(em.id.eq(ec.exchangeId))
            .join(baseCoin).on(baseCoin.id.eq(em.baseCurrencyCoinId))
            .where(ec.id.in(exchangeCoinIds))
            .fetch();

        Map<Long, ExchangeSymbolKey> map = new LinkedHashMap<>();
        for (Tuple row : rows) {
            Long exchangeCoinId = row.get(ec.id);
            ExchangeSymbolKey key = ExchangeSymbolKey.of(
                row.get(em.name),
                row.get(ec.displayName),
                row.get(baseCoin.symbol)
            );
            map.put(exchangeCoinId, key);
        }
        return new MarketSymbols(map);
    }
}
