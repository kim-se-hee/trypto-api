package ksh.tryptobackend.investmentround.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

public interface FundingWalletPort {

    Optional<Long> findWalletId(Long roundId, Long exchangeId);

    void addBalance(Long walletId, Long coinId, BigDecimal amount);
}
