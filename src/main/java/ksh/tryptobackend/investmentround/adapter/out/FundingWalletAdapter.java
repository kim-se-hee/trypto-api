package ksh.tryptobackend.investmentround.adapter.out;

import ksh.tryptobackend.investmentround.application.port.out.FundingWalletPort;
import ksh.tryptobackend.wallet.application.port.out.WalletBalanceOperationPort;
import ksh.tryptobackend.wallet.application.port.out.WalletQueryPort;
import ksh.tryptobackend.wallet.application.port.out.dto.WalletInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FundingWalletAdapter implements FundingWalletPort {

    private final WalletQueryPort walletQueryPort;
    private final WalletBalanceOperationPort walletBalanceOperationPort;

    @Override
    public Optional<Long> findWalletId(Long roundId, Long exchangeId) {
        return walletQueryPort.findByRoundIdAndExchangeId(roundId, exchangeId)
            .map(WalletInfo::walletId);
    }

    @Override
    public void addBalance(Long walletId, Long coinId, BigDecimal amount) {
        walletBalanceOperationPort.addBalance(walletId, coinId, amount);
    }
}
