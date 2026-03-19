package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.model.Exchange;
import ksh.tryptobackend.marketdata.domain.model.ExchangeMarketType;

import java.math.BigDecimal;

public interface ExchangeCommandPort {

    Exchange save(String name, ExchangeMarketType marketType, Long baseCurrencyCoinId, BigDecimal feeRate);
}
