package ksh.tryptobackend.trading.application.port.in.dto.command;

import ksh.tryptobackend.trading.domain.vo.OrderType;
import ksh.tryptobackend.trading.domain.vo.Side;

import java.math.BigDecimal;

public record PlaceOrderCommand(
    String idempotencyKey,
    Long walletId,
    Long exchangeCoinId,
    Side side,
    OrderType orderType,
    BigDecimal price,
    BigDecimal amount
) {
}
