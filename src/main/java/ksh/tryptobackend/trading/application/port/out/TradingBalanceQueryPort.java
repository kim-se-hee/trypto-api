package ksh.tryptobackend.trading.application.port.out;

import java.math.BigDecimal;

public interface TradingBalanceQueryPort {

    BigDecimal getAvailableBalance(Long walletId, Long coinId);
}
