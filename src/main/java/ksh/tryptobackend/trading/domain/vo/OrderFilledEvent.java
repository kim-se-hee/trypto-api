package ksh.tryptobackend.trading.domain.vo;

import java.math.BigDecimal;

public record OrderFilledEvent(
    String eventType,
    Long walletId,
    Long orderId,
    Long coinId,
    Side side,
    BigDecimal quantity,
    BigDecimal price,
    BigDecimal fee
) {

    public static OrderFilledEvent from(Long walletId, Long orderId, Long coinId,
                                        Side side, BigDecimal quantity,
                                        BigDecimal price, BigDecimal fee) {
        return new OrderFilledEvent("ORDER_FILLED", walletId, orderId, coinId,
            side, quantity, price, fee);
    }
}
