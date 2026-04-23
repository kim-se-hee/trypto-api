package ksh.tryptobackend.marketdata.application.service;

import ksh.tryptobackend.marketdata.application.port.in.FindExchangeNamesUseCase;
import ksh.tryptobackend.marketdata.application.port.out.ExchangeQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FindExchangeNamesService implements FindExchangeNamesUseCase {

    private final ExchangeQueryPort exchangeQueryPort;

    @Override
    public Map<Long, String> findExchangeNames(Set<Long> exchangeIds) {
        return exchangeQueryPort.findNamesByIds(exchangeIds);
    }
}
