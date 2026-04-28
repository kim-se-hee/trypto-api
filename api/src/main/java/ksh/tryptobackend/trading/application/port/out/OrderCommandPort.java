package ksh.tryptobackend.trading.application.port.out;

import ksh.tryptobackend.trading.domain.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderCommandPort {

    Order save(Order order);

    Optional<Order> findById(Long orderId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    long countByWalletIdAndCreatedAtBetween(Long walletId, LocalDateTime from, LocalDateTime to);

    boolean fillOrder(Long orderId, BigDecimal filledPrice, LocalDateTime filledAt);

    boolean cancelOrder(Long orderId);
}
