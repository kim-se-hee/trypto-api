package ksh.tryptobackend.regretanalysis.domain.strategy;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationLossContext;

import java.math.BigDecimal;

public class SellViolationLossStrategy implements ViolationLossStrategy {

    @Override
    public boolean supports(RuleType ruleType, boolean isBuy) {
        return ruleType == RuleType.OVERTRADING_LIMIT && !isBuy;
    }

    @Override
    public BigDecimal calculateLoss(ViolationLossContext context) {
        return context.currentPrice().subtract(context.filledPrice()).multiply(context.quantity());
    }
}
