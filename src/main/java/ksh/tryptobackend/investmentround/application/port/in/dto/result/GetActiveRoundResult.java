package ksh.tryptobackend.investmentround.application.port.in.dto.result;

import ksh.tryptobackend.investmentround.domain.vo.RoundOverview;
import ksh.tryptobackend.investmentround.domain.model.RuleSetting;
import ksh.tryptobackend.investmentround.domain.vo.RoundStatus;
import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetActiveRoundResult(
    Long roundId,
    Long userId,
    long roundNumber,
    RoundStatus status,
    BigDecimal initialSeed,
    BigDecimal emergencyFundingLimit,
    int emergencyChargeCount,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    List<GetActiveRoundRuleResult> rules,
    List<GetActiveRoundWalletResult> wallets
) {

    public static GetActiveRoundResult from(RoundOverview round, List<RuleSetting> rules,
                                             List<WalletResult> walletResults) {
        List<GetActiveRoundRuleResult> ruleResults = rules.stream()
            .map(GetActiveRoundRuleResult::from)
            .toList();

        List<GetActiveRoundWalletResult> wallets = walletResults.stream()
            .map(GetActiveRoundWalletResult::from)
            .toList();

        return new GetActiveRoundResult(
            round.roundId(),
            round.userId(),
            round.roundNumber(),
            round.status(),
            round.initialSeed(),
            round.emergencyFundingLimit(),
            round.emergencyChargeCount(),
            round.startedAt(),
            round.endedAt(),
            ruleResults,
            wallets
        );
    }
}
