package ksh.tryptobackend.regretanalysis.domain.strategy;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationLossContext;

import java.math.BigDecimal;

public class ProfitTakeViolationLossStrategy implements ViolationLossStrategy {

    @Override
    public boolean supports(RuleType ruleType, boolean isBuy) {
        return ruleType == RuleType.PROFIT_TAKE;
    }

    @Override
    public BigDecimal calculateLoss(ViolationLossContext context) {
        return context.tradeAmount().subtract(context.currentPrice().multiply(context.quantity()));
    }
}
