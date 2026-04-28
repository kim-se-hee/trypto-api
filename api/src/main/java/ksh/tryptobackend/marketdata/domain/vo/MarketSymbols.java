package ksh.tryptobackend.marketdata.domain.vo;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;

import java.util.Map;

public record MarketSymbols(Map<Long, ExchangeSymbolKey> byExchangeCoinId) {

    public ExchangeSymbolKey require(Long exchangeCoinId) {
        ExchangeSymbolKey key = byExchangeCoinId.get(exchangeCoinId);
        if (key == null) {
            throw new CustomException(ErrorCode.EXCHANGE_COIN_NOT_FOUND);
        }
        return key;
    }

    public boolean contains(Long exchangeCoinId) {
        return byExchangeCoinId.containsKey(exchangeCoinId);
    }
}
