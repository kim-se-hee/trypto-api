package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeCoinChainResult;

import java.util.Optional;

public interface FindExchangeCoinChainUseCase {

    Optional<ExchangeCoinChainResult> findByExchangeIdAndCoinIdAndChain(Long exchangeId, Long coinId, String chain);
}
