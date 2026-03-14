package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.model.ExchangeCoinChain;

import java.util.Optional;

public interface ExchangeCoinChainQueryPort {

    Optional<ExchangeCoinChain> findByExchangeIdAndCoinIdAndChain(Long exchangeId, Long coinId, String chain);
}
