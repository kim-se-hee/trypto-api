package ksh.tryptobackend.marketdata.application.service;

import ksh.tryptobackend.marketdata.application.port.in.FindMarketSymbolsByIdsUseCase;
import ksh.tryptobackend.marketdata.application.port.out.ExchangeCoinQueryPort;
import ksh.tryptobackend.marketdata.domain.vo.MarketSymbols;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FindMarketSymbolsByIdsService implements FindMarketSymbolsByIdsUseCase {

    private final ExchangeCoinQueryPort exchangeCoinQueryPort;

    @Override
    public MarketSymbols findByIds(Collection<Long> exchangeCoinIds) {
        return exchangeCoinQueryPort.findMarketSymbolsByIds(exchangeCoinIds);
    }
}
