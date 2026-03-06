package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeDetailResult;

import java.util.Optional;

public interface FindExchangeDetailUseCase {

    Optional<ExchangeDetailResult> findExchangeDetail(Long exchangeId);
}
