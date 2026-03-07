package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindCoinSymbolsUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeDetailResult;
import ksh.tryptobackend.regretanalysis.application.port.out.AnalysisExchangeQueryPort;
import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisExchange;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AnalysisExchangeQueryAdapter implements AnalysisExchangeQueryPort {

    private final FindExchangeDetailUseCase findExchangeDetailUseCase;
    private final FindCoinSymbolsUseCase findCoinSymbolsUseCase;
    private final FindWalletUseCase findWalletUseCase;

    @Override
    public AnalysisExchange getExchangeInfo(Long exchangeId) {
        ExchangeDetailResult detail = findExchangeDetailUseCase.findExchangeDetail(exchangeId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));

        return new AnalysisExchange(exchangeId, detail.name(), detail.domestic() ? "KRW" : "USDT");
    }

    @Override
    public boolean existsWalletForExchange(Long roundId, Long exchangeId) {
        return findWalletUseCase.findByRoundIdAndExchangeId(roundId, exchangeId).isPresent();
    }

    @Override
    public Map<Long, String> findCoinSymbolsByIds(Set<Long> coinIds) {
        return findCoinSymbolsUseCase.findSymbolsByIds(coinIds);
    }
}
