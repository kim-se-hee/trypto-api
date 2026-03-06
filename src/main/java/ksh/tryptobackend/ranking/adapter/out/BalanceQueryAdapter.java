package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.ranking.application.port.out.BalanceQueryPort;
import ksh.tryptobackend.wallet.application.port.in.GetAvailableBalanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("rankingBalanceQueryAdapter")
@RequiredArgsConstructor
public class BalanceQueryAdapter implements BalanceQueryPort {

    private final GetAvailableBalanceUseCase getAvailableBalanceUseCase;

    @Override
    public BigDecimal getAvailableBalance(Long walletId, Long coinId) {
        return getAvailableBalanceUseCase.getAvailableBalance(walletId, coinId);
    }
}
