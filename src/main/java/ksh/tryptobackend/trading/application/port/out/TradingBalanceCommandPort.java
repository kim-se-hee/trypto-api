package ksh.tryptobackend.trading.application.port.out;

import java.math.BigDecimal;

public interface TradingBalanceCommandPort {

    void deductBalance(Long walletId, Long coinId, BigDecimal amount);

    void addBalance(Long walletId, Long coinId, BigDecimal amount);

    void lockBalance(Long walletId, Long coinId, BigDecimal amount);

    void unlockBalance(Long walletId, Long coinId, BigDecimal amount);
}
