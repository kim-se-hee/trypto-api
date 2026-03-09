package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinMappingUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.trading.application.port.in.CancelOrderUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.command.CancelOrderCommand;
import ksh.tryptobackend.trading.application.port.out.OrderCommandPort;
import ksh.tryptobackend.trading.domain.model.Order;
import ksh.tryptobackend.trading.domain.vo.ListedCoinRef;
import ksh.tryptobackend.trading.domain.vo.OrderAmountPolicy;
import ksh.tryptobackend.trading.domain.vo.Side;
import ksh.tryptobackend.trading.domain.vo.TradingVenue;
import ksh.tryptobackend.wallet.application.port.in.ManageWalletBalanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderCommandPort orderCommandPort;
    private final ManageWalletBalanceUseCase manageWalletBalanceUseCase;
    private final FindExchangeDetailUseCase findExchangeDetailUseCase;
    private final FindExchangeCoinMappingUseCase findExchangeCoinMappingUseCase;

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
        ListedCoinRef listedCoin = getListedCoin(order.getExchangeCoinId());

        if (order.getSide() == Side.BUY) {
            TradingVenue venue = getTradingVenue(listedCoin.exchangeId());
            manageWalletBalanceUseCase.unlockBalance(order.getWalletId(), venue.baseCurrencyCoinId(), order.getSettlementDebit());
        } else {
            manageWalletBalanceUseCase.unlockBalance(order.getWalletId(), listedCoin.coinId(), order.getQuantity().value());
        }
    }

    private ListedCoinRef getListedCoin(Long exchangeCoinId) {
        return findExchangeCoinMappingUseCase.findById(exchangeCoinId)
            .map(m -> new ListedCoinRef(m.exchangeCoinId(), m.exchangeId(), m.coinId()))
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_COIN_NOT_FOUND));
    }

    private TradingVenue getTradingVenue(Long exchangeId) {
        return findExchangeDetailUseCase.findExchangeDetail(exchangeId)
            .map(detail -> new TradingVenue(
                detail.feeRate(),
                detail.baseCurrencyCoinId(),
                detail.domestic() ? OrderAmountPolicy.DOMESTIC : OrderAmountPolicy.OVERSEAS))
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
    }
}
