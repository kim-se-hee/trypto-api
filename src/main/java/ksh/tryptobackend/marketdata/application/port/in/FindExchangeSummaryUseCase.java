package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeSummaryResult;

import java.util.Optional;

public interface FindExchangeSummaryUseCase {

    Optional<ExchangeSummaryResult> findExchangeSummary(Long exchangeId);
}
