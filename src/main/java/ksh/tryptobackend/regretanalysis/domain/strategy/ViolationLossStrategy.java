package ksh.tryptobackend.regretanalysis.domain.strategy;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationLossContext;

import java.math.BigDecimal;
import java.util.List;

public interface ViolationLossStrategy {

    boolean supports(RuleType ruleType, boolean isBuy);

    BigDecimal calculateLoss(ViolationLossContext context);

    static List<ViolationLossStrategy> all() {
        return List.of(
            new BuyViolationLossStrategy(),
            new SellViolationLossStrategy(),
            new LossCutViolationLossStrategy(),
            new ProfitTakeViolationLossStrategy()
        );
    }
}
