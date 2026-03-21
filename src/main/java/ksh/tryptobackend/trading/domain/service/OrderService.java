package ksh.tryptobackend.trading.domain.service;

import ksh.tryptobackend.trading.application.port.out.OrderCommandPort;
import ksh.tryptobackend.trading.application.port.out.OrderPlacedEventPort;
import ksh.tryptobackend.trading.domain.model.Order;
import ksh.tryptobackend.trading.domain.vo.OrderPlacedEvent;
import ksh.tryptobackend.trading.domain.vo.TradingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderService {

    private final OrderCommandPort orderCommandPort;
    private final OrderPlacedEventPort orderPlacedEventPort;

    public Order findDuplicate(String idempotencyKey) {
        return orderCommandPort.findByIdempotencyKey(idempotencyKey).orElse(null);
    }

    public Order save(Order order, TradingContext ctx) {
        Order saved = orderCommandPort.save(order);
        orderPlacedEventPort.publish(OrderPlacedEvent.of(saved, ctx));
        return saved;
    }
}
