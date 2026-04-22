package ksh.tryptobackend.trading.application.port.out;

import ksh.tryptobackend.trading.domain.vo.OrderFilledNotification;

public interface OrderFilledNotificationPort {

    void push(OrderFilledNotification notification);
}
