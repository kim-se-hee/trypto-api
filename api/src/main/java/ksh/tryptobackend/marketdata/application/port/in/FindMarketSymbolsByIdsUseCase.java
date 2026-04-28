package ksh.tryptobackend.marketdata.application.port.in;

import ksh.tryptobackend.marketdata.domain.vo.MarketSymbols;

import java.util.Collection;

public interface FindMarketSymbolsByIdsUseCase {

    MarketSymbols findByIds(Collection<Long> exchangeCoinIds);
}
