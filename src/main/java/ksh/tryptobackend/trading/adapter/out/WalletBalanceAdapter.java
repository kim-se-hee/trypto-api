package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.trading.application.port.out.WalletBalancePort;
import ksh.tryptobackend.wallet.application.port.in.GetAvailableBalanceUseCase;
import ksh.tryptobackend.wallet.application.port.in.ManageWalletBalanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WalletBalanceAdapter implements WalletBalancePort {

    private final GetAvailableBalanceUseCase getAvailableBalanceUseCase;
    private final ManageWalletBalanceUseCase manageWalletBalanceUseCase;

    @Override
    public BigDecimal getAvailableBalance(Long walletId, Long coinId) {
        return getAvailableBalanceUseCase.getAvailableBalance(walletId, coinId);
    }

    @Override
    public void deductBalance(Long walletId, Long coinId, BigDecimal amount) {
        manageWalletBalanceUseCase.deductBalance(walletId, coinId, amount);
    }

    @Override
    public void addBalance(Long walletId, Long coinId, BigDecimal amount) {
        manageWalletBalanceUseCase.addBalance(walletId, coinId, amount);
    }

    @Override
    public void lockBalance(Long walletId, Long coinId, BigDecimal amount) {
        manageWalletBalanceUseCase.lockBalance(walletId, coinId, amount);
    }

    @Override
    public void unlockBalance(Long walletId, Long coinId, BigDecimal amount) {
        manageWalletBalanceUseCase.unlockBalance(walletId, coinId, amount);
    }
}
