package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.marketdata.application.port.in.FindAllExchangeIdsUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindCoinInfoUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinsUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.CoinInfoResult;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeCoinListResult;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeDetailResult;
import ksh.tryptobackend.trading.application.port.in.WarmupPendingOrderMatchingUseCase;
import ksh.tryptobackend.trading.application.port.out.ExchangeCoinMappingCacheCommandPort;
import ksh.tryptobackend.trading.application.port.out.OrderQueryPort;
import ksh.tryptobackend.trading.application.port.out.PendingOrderCacheCommandPort;
import ksh.tryptobackend.trading.domain.vo.ExchangeSymbolKey;
import ksh.tryptobackend.trading.domain.vo.PendingOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarmupPendingOrderMatchingService implements WarmupPendingOrderMatchingUseCase {

    private final ExchangeCoinMappingCacheCommandPort exchangeCoinMappingCacheCommandPort;
    private final PendingOrderCacheCommandPort pendingOrderCacheCommandPort;
    private final OrderQueryPort orderQueryPort;

    private final FindAllExchangeIdsUseCase findAllExchangeIdsUseCase;
    private final FindExchangeDetailUseCase findExchangeDetailUseCase;
    private final FindExchangeCoinsUseCase findExchangeCoinsUseCase;
    private final FindCoinInfoUseCase findCoinInfoUseCase;

    @Override
    public void warmup() {
        loadExchangeCoinMappingCache();
        loadPendingOrderCache();
    }

    private void loadExchangeCoinMappingCache() {
        List<Long> exchangeIds = findAllExchangeIdsUseCase.findAllExchangeIds();
        Map<ExchangeSymbolKey, Long> mappings = new HashMap<>();

        for (Long exchangeId : exchangeIds) {
            buildMappingsForExchange(exchangeId, mappings);
        }

        exchangeCoinMappingCacheCommandPort.loadAll(mappings);
        log.info("거래소-코인 매핑 캐시 로딩 완료: {}건", mappings.size());
    }

    private void buildMappingsForExchange(Long exchangeId, Map<ExchangeSymbolKey, Long> mappings) {
        ExchangeDetailResult detail = findExchangeDetailUseCase.findExchangeDetail(exchangeId)
            .orElse(null);
        if (detail == null) {
            return;
        }

        String baseCurrencySymbol = resolveBaseCurrencySymbol(detail.baseCurrencyCoinId());
        if (baseCurrencySymbol == null) {
            return;
        }

        List<ExchangeCoinListResult> exchangeCoins = findExchangeCoinsUseCase.findByExchangeId(exchangeId);
        for (ExchangeCoinListResult ec : exchangeCoins) {
            String symbol = ec.coinSymbol() + "/" + baseCurrencySymbol;
            mappings.put(new ExchangeSymbolKey(detail.name(), symbol), ec.exchangeCoinId());
        }
    }

    private String resolveBaseCurrencySymbol(Long baseCurrencyCoinId) {
        Map<Long, CoinInfoResult> coinInfoMap = findCoinInfoUseCase.findByIds(Set.of(baseCurrencyCoinId));
        CoinInfoResult coinInfo = coinInfoMap.get(baseCurrencyCoinId);
        return coinInfo != null ? coinInfo.symbol() : null;
    }

    private void loadPendingOrderCache() {
        List<PendingOrder> pendingOrders = orderQueryPort.findAllPendingOrders();
        pendingOrderCacheCommandPort.addAll(pendingOrders);
        log.info("미체결 주문 캐시 로딩 완료: {}건", pendingOrders.size());
    }
}
