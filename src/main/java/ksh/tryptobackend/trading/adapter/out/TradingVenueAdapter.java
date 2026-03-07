package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.trading.application.port.out.TradingVenuePort;
import ksh.tryptobackend.trading.domain.vo.OrderAmountPolicy;
import ksh.tryptobackend.trading.domain.vo.TradingVenue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TradingVenueAdapter implements TradingVenuePort {

    private final FindExchangeDetailUseCase findExchangeDetailUseCase;

    @Override
    public Optional<TradingVenue> findByExchangeId(Long exchangeId) {
        return findExchangeDetailUseCase.findExchangeDetail(exchangeId)
            .map(detail -> new TradingVenue(
                detail.feeRate(),
                detail.baseCurrencyCoinId(),
                detail.domestic() ? OrderAmountPolicy.DOMESTIC : OrderAmountPolicy.OVERSEAS));
    }
}
