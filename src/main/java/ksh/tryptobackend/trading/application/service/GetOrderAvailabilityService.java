package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.trading.application.port.in.GetOrderAvailabilityUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.query.GetOrderAvailabilityQuery;
import ksh.tryptobackend.trading.application.port.in.dto.result.OrderAvailabilityResult;
import ksh.tryptobackend.trading.application.port.out.ListedCoinQueryPort;
import ksh.tryptobackend.trading.application.port.out.LivePriceQueryPort;
import ksh.tryptobackend.trading.application.port.out.TradingVenueQueryPort;
import ksh.tryptobackend.trading.application.port.out.TradingBalanceQueryPort;
import ksh.tryptobackend.trading.domain.vo.ListedCoinRef;
import ksh.tryptobackend.trading.domain.vo.Side;
import ksh.tryptobackend.trading.domain.vo.TradingVenue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GetOrderAvailabilityService implements GetOrderAvailabilityUseCase {

    private final TradingBalanceQueryPort tradingBalanceQueryPort;
    private final LivePriceQueryPort livePriceQueryPort;
    private final TradingVenueQueryPort tradingVenueQueryPort;
    private final ListedCoinQueryPort listedCoinQueryPort;

    @Override
    @Transactional(readOnly = true)
    public OrderAvailabilityResult getAvailability(GetOrderAvailabilityQuery query) {
        ListedCoinRef listedCoin = getListedCoin(query.exchangeCoinId());
        TradingVenue venue = getTradingVenue(listedCoin.exchangeId());

        BigDecimal available = getAvailableBalance(query.walletId(), query.side(), venue, listedCoin);
        BigDecimal currentPrice = livePriceQueryPort.getCurrentPrice(query.exchangeCoinId());

        return new OrderAvailabilityResult(available, currentPrice);
    }

    private ListedCoinRef getListedCoin(Long exchangeCoinId) {
        return listedCoinQueryPort.findById(exchangeCoinId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_COIN_NOT_FOUND));
    }

    private TradingVenue getTradingVenue(Long exchangeId) {
        return tradingVenueQueryPort.findByExchangeId(exchangeId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
    }

    private BigDecimal getAvailableBalance(Long walletId, Side side,
                                            TradingVenue venue, ListedCoinRef listedCoin) {
        Long targetCoinId = side == Side.BUY
            ? venue.baseCurrencyCoinId()
            : listedCoin.coinId();
        return tradingBalanceQueryPort.getAvailableBalance(walletId, targetCoinId);
    }
}
