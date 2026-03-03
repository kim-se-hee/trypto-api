package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.application.port.out.dto.ExchangeCoinChainInfo;

import java.util.Optional;

public interface ExchangeCoinChainQueryPort {

    Optional<ExchangeCoinChainInfo> findByExchangeIdAndCoinIdAndChain(Long exchangeId, Long coinId, String chain);
}
