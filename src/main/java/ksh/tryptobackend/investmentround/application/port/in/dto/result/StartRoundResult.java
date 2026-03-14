package ksh.tryptobackend.investmentround.application.port.in.dto.result;

import ksh.tryptobackend.investmentround.domain.model.InvestmentRound;
import ksh.tryptobackend.investmentround.domain.vo.RoundStatus;
import ksh.tryptobackend.wallet.application.port.in.dto.result.WalletResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StartRoundResult(
    Long roundId,
    long roundNumber,
    RoundStatus status,
    BigDecimal initialSeed,
    BigDecimal emergencyFundingLimit,
    int emergencyChargeCount,
    List<StartRoundRuleResult> rules,
    List<StartRoundWalletResult> wallets,
    LocalDateTime startedAt
) {

    public static StartRoundResult from(InvestmentRound round, List<WalletResult> walletResults) {
        List<StartRoundRuleResult> ruleResults = round.getRules().stream()
            .map(StartRoundRuleResult::from)
            .toList();

        List<StartRoundWalletResult> wallets = walletResults.stream()
            .map(StartRoundWalletResult::from)
            .toList();

        return new StartRoundResult(
            round.getRoundId(),
            round.getRoundNumber(),
            round.getStatus(),
            round.getInitialSeed(),
            round.getEmergencyFundingLimit(),
            round.getEmergencyChargeCount(),
            ruleResults,
            wallets,
            round.getStartedAt()
        );
    }
}
