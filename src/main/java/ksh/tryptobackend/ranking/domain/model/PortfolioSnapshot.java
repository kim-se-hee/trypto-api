package ksh.tryptobackend.ranking.domain.model;

import ksh.tryptobackend.ranking.domain.vo.KrwConversionRate;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Getter
@Builder
public class PortfolioSnapshot {

    private static final int RATE_SCALE = 4;

    private final Long id;
    private final Long userId;
    private final Long roundId;
    private final Long exchangeId;
    private final BigDecimal totalAsset;
    private final BigDecimal totalAssetKrw;
    private final BigDecimal totalInvestment;
    private final BigDecimal totalProfit;
    private final BigDecimal totalProfitRate;
    private final LocalDate snapshotDate;

    public static PortfolioSnapshot create(Long userId, Long roundId, Long exchangeId,
                                           BigDecimal totalAsset, BigDecimal totalInvestment,
                                           KrwConversionRate conversionRate, LocalDate snapshotDate) {
        BigDecimal totalAssetKrw = conversionRate.convert(totalAsset);
        BigDecimal totalProfit = totalAsset.subtract(totalInvestment);
        BigDecimal totalProfitRate = calculateProfitRate(totalAsset, totalInvestment);

        return PortfolioSnapshot.builder()
            .userId(userId)
            .roundId(roundId)
            .exchangeId(exchangeId)
            .totalAsset(totalAsset)
            .totalAssetKrw(totalAssetKrw)
            .totalInvestment(totalInvestment)
            .totalProfit(totalProfit)
            .totalProfitRate(totalProfitRate)
            .snapshotDate(snapshotDate)
            .build();
    }

    private static BigDecimal calculateProfitRate(BigDecimal totalAsset, BigDecimal totalInvestment) {
        if (totalInvestment.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalAsset.subtract(totalInvestment)
            .divide(totalInvestment, RATE_SCALE, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }
}
