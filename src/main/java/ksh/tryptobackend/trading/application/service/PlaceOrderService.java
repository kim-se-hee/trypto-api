package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.trading.application.port.in.PlaceOrderUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.command.PlaceOrderCommand;
import ksh.tryptobackend.trading.domain.event.OrderPlacedEvent;
import ksh.tryptobackend.trading.domain.model.Order;
import ksh.tryptobackend.trading.domain.service.Balances;
import ksh.tryptobackend.trading.domain.service.OrderService;
import ksh.tryptobackend.trading.domain.service.TradingContextResolver;
import ksh.tryptobackend.trading.domain.service.TradingRules;
import ksh.tryptobackend.trading.domain.vo.TradingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderService orderService;
    private final TradingContextResolver tradingContextResolver;
    private final Balances balances;
    private final TradingRules rules;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Order placeOrder(PlaceOrderCommand cmd) {
        Order duplicate = orderService.findDuplicate(cmd.idempotencyKey());
        if (duplicate != null) return duplicate;

        TradingContext ctx = tradingContextResolver.resolve(cmd);
        Order order = Order.create(cmd, ctx);

        balances.validateFor(order, ctx);
        rules.inspect(order, ctx);

        Order saved = orderService.save(order, ctx);

        if (saved.shouldForwardToEngine()) {
            eventPublisher.publishEvent(new OrderPlacedEvent(saved));
        }
        return saved;
    }
}
