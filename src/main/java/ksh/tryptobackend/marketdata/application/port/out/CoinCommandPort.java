package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.model.Coin;

public interface CoinCommandPort {

    Coin save(String symbol, String name);
}
