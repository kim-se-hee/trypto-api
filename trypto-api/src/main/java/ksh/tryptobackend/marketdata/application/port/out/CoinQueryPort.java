package ksh.tryptobackend.marketdata.application.port.out;

import ksh.tryptobackend.marketdata.domain.model.Coin;
import ksh.tryptobackend.marketdata.domain.vo.CoinSymbols;

import java.util.List;
import java.util.Set;

public interface CoinQueryPort {

    CoinSymbols findSymbolsByIds(Set<Long> coinIds);

    List<Coin> findByIds(Set<Long> coinIds);

}
