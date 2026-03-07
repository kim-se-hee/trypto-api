package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinMappingUseCase;
import ksh.tryptobackend.trading.application.port.out.ListedCoinPort;
import ksh.tryptobackend.trading.domain.vo.ListedCoinRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ListedCoinAdapter implements ListedCoinPort {

    private final FindExchangeCoinMappingUseCase findExchangeCoinMappingUseCase;

    @Override
    public Optional<ListedCoinRef> findById(Long exchangeCoinId) {
        return findExchangeCoinMappingUseCase.findById(exchangeCoinId)
            .map(m -> new ListedCoinRef(m.exchangeCoinId(), m.exchangeId(), m.coinId()));
    }
}
