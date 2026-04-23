package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.model.ExchangeCoin;

public interface ExchangeCoinCommandPort {

    ExchangeCoin save(Long exchangeId, Long coinId, String displayName);
}
