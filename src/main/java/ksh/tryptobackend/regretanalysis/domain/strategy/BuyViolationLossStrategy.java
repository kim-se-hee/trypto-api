package ksh.tryptobackend.regretanalysis.domain.strategy;

import ksh.tryptobackend.common.domain.vo.RuleType;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationLossContext;
import ksh.tryptobackend.regretanalysis.domain.vo.ViolationLossContext.SoldPortion;

import java.math.BigDecimal;

public class BuyViolationLossStrategy implements ViolationLossStrategy {

    @Override
    public boolean supports(RuleType ruleType, boolean isBuy) {
        return switch (ruleType) {
            case CHASE_BUY_BAN, AVERAGING_DOWN_LIMIT -> true;
            case OVERTRADING_LIMIT -> isBuy;
            default -> false;
        };
    }

    @Override
    public BigDecimal calculateLoss(ViolationLossContext context) {
        BigDecimal remainingQty = context.quantity();
        BigDecimal totalLoss = BigDecimal.ZERO;

        for (SoldPortion sell : context.soldPortions()) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal matchedQty = sell.quantity().min(remainingQty);
            totalLoss = totalLoss.add(
                context.filledPrice().subtract(sell.price()).multiply(matchedQty));
            remainingQty = remainingQty.subtract(matchedQty);
        }

        if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
            totalLoss = totalLoss.add(
                context.filledPrice().subtract(context.currentPrice()).multiply(remainingQty));
        }

        return totalLoss;
    }
}
