package ksh.tryptobackend.trading.domain.vo;

public record OrderFilledNotification(
    Long userId,
    OrderFilledEvent event
) {
}
