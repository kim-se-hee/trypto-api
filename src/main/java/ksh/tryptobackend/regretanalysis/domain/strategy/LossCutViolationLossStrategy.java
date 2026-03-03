package ksh.tryptobackend.regretanalysis.domain.strategy;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationLossContext;

import java.math.BigDecimal;

public class LossCutViolationLossStrategy implements ViolationLossStrategy {

    @Override
    public boolean supports(RuleType ruleType, boolean isBuy) {
        return ruleType == RuleType.LOSS_CUT;
    }

    @Override
    public BigDecimal calculateLoss(ViolationLossContext context) {
        return context.currentPrice().multiply(context.quantity()).subtract(context.tradeAmount());
    }
}
