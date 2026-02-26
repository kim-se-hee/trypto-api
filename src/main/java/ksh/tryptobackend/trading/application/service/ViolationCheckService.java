package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.trading.application.port.out.*;
import ksh.tryptobackend.trading.application.port.out.HoldingPort.HoldingData;
import ksh.tryptobackend.trading.application.port.out.InvestmentRulePort.InvestmentRuleData;
import ksh.tryptobackend.trading.domain.model.Order;
import ksh.tryptobackend.trading.domain.model.RuleViolation;
import ksh.tryptobackend.trading.domain.model.ViolationChecker;
import ksh.tryptobackend.trading.domain.vo.Side;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ViolationCheckService {

    private final WalletInfoPort walletInfoPort;
    private final InvestmentRulePort investmentRulePort;
    private final HoldingPort holdingPort;
    private final PriceChangeRatePort priceChangeRatePort;
    private final OrderPersistencePort orderPersistencePort;
    private final Clock clock;

    public List<RuleViolation> checkOrderViolations(Order order, Long walletId,
                                                    Long exchangeCoinId, Long coinId,
                                                    BigDecimal currentPrice) {
        Long roundId = walletInfoPort.getRoundIdByWalletId(walletId);
        List<InvestmentRuleData> rules = investmentRulePort.findByRoundId(roundId);
        if (rules.isEmpty()) {
            return List.of();
        }

        HoldingData holding = holdingPort.findByWalletIdAndCoinId(walletId, coinId)
            .orElse(null);

        BigDecimal changeRate = BigDecimal.ZERO;
        if (order.getSide() == Side.BUY) {
            changeRate = priceChangeRatePort.getChangeRate(exchangeCoinId);
        }

        LocalDate today = LocalDate.now(clock);
        long todayOrderCount = orderPersistencePort.countByWalletIdAndCreatedAtBetween(
            walletId, today.atStartOfDay(), today.atTime(23, 59, 59));

        LocalDateTime now = LocalDateTime.now(clock);
        return ViolationChecker.check(order, rules, holding, changeRate, currentPrice,
            todayOrderCount, now);
    }
}
