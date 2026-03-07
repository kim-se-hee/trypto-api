package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.trading.application.port.in.CancelOrderUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.command.CancelOrderCommand;
import ksh.tryptobackend.trading.application.port.out.ListedCoinQueryPort;
import ksh.tryptobackend.trading.application.port.out.OrderCommandPort;
import ksh.tryptobackend.trading.application.port.out.TradingVenueQueryPort;
import ksh.tryptobackend.trading.application.port.out.TradingBalanceCommandPort;
import ksh.tryptobackend.trading.domain.model.Order;
import ksh.tryptobackend.trading.domain.vo.ListedCoinRef;
import ksh.tryptobackend.trading.domain.vo.Side;
import ksh.tryptobackend.trading.domain.vo.TradingVenue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderCommandPort orderCommandPort;
    private final TradingBalanceCommandPort tradingBalanceCommandPort;
    private final TradingVenueQueryPort tradingVenueQueryPort;
    private final ListedCoinQueryPort listedCoinQueryPort;

    @Override
    @Transactional
    public Order cancelOrder(CancelOrderCommand command) {
        Order order = getOrder(command.orderId());

        if (order.isAlreadyCancelled()) {
            return order;
        }

        order.cancel();
        unlockBalance(order);

        return orderCommandPort.save(order);
    }

    private Order getOrder(Long orderId) {
        return orderCommandPort.findById(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    private void unlockBalance(Order order) {
        ListedCoinRef listedCoin = listedCoinQueryPort.findById(order.getExchangeCoinId())
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_COIN_NOT_FOUND));

        if (order.getSide() == Side.BUY) {
            TradingVenue venue = tradingVenueQueryPort.findByExchangeId(listedCoin.exchangeId())
                .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
            tradingBalanceCommandPort.unlockBalance(order.getWalletId(), venue.baseCurrencyCoinId(), order.getSettlementDebit());
        } else {
            tradingBalanceCommandPort.unlockBalance(order.getWalletId(), listedCoin.coinId(), order.getQuantity().value());
        }
    }
}
