package ksh.tryptobackend.marketdata.application.service;

import ksh.tryptobackend.marketdata.application.port.in.FindExchangeSummaryUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeSummaryResult;
import ksh.tryptobackend.marketdata.application.port.out.ExchangeQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FindExchangeSummaryService implements FindExchangeSummaryUseCase {

    private final ExchangeQueryPort exchangeQueryPort;

    @Override
    public Optional<ExchangeSummaryResult> findExchangeSummary(Long exchangeId) {
        return exchangeQueryPort.findExchangeSummaryById(exchangeId)
            .map(summary -> new ExchangeSummaryResult(
                summary.exchangeId(), summary.name(), summary.baseCurrencySymbol()));
    }
}
