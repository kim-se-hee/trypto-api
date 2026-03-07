package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.FindInvestmentRulesUseCase;
import ksh.tryptobackend.trading.application.port.out.ViolationRulePort;
import ksh.tryptobackend.trading.domain.model.ViolationRule;
import ksh.tryptobackend.wallet.application.port.in.FindWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ViolationRuleAdapter implements ViolationRulePort {

    private final FindWalletUseCase findWalletUseCase;
    private final FindInvestmentRulesUseCase findInvestmentRulesUseCase;

    @Override
    public List<ViolationRule> findByWalletId(Long walletId) {
        return findWalletUseCase.findById(walletId)
            .map(wallet -> findInvestmentRulesUseCase.findByRoundId(wallet.roundId()).stream()
                .map(r -> ViolationRule.of(r.ruleId(), r.ruleType(), r.thresholdValue()))
                .toList())
            .orElse(List.of());
    }
}
