package ksh.tryptobackend.regretanalysis.domain.vo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BtcBenchmark {

    private static final int PRICE_SCALE = 8;
    private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

    private final Map<LocalDate, BigDecimal> assetByDate;

    private BtcBenchmark(Map<LocalDate, BigDecimal> assetByDate) {
        this.assetByDate = assetByDate;
    }

    public static BtcBenchmark calculate(BigDecimal seedMoney,
                                          Map<LocalDate, BigDecimal> btcPriceByDate,
                                          List<LocalDate> snapshotDates,
                                          LocalDate startDate) {
        BigDecimal btcPriceAtStart = btcPriceByDate.get(startDate);
        if (btcPriceAtStart == null || btcPriceAtStart.compareTo(BigDecimal.ZERO) == 0) {
            return new BtcBenchmark(Map.of());
        }

        BigDecimal btcQuantity = seedMoney.divide(btcPriceAtStart, PRICE_SCALE, RoundingMode.HALF_UP);

        Map<LocalDate, BigDecimal> result = new HashMap<>();
        for (LocalDate date : snapshotDates) {
            BigDecimal dailyPrice = btcPriceByDate.get(date);
            if (dailyPrice == null) {
                result.put(date, BigDecimal.ZERO);
            } else {
                result.put(date, btcQuantity.multiply(dailyPrice, MATH_CONTEXT));
            }
        }
        return new BtcBenchmark(result);
    }

    public BigDecimal getAssetValueAt(LocalDate date) {
        return assetByDate.getOrDefault(date, BigDecimal.ZERO);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BtcBenchmark that)) return false;
        if (assetByDate.size() != that.assetByDate.size()) return false;
        for (Map.Entry<LocalDate, BigDecimal> entry : assetByDate.entrySet()) {
            BigDecimal otherValue = that.assetByDate.get(entry.getKey());
            if (otherValue == null || entry.getValue().compareTo(otherValue) != 0) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(assetByDate.size());
        for (Map.Entry<LocalDate, BigDecimal> entry : assetByDate.entrySet()) {
            result = 31 * result + Objects.hash(entry.getKey(), entry.getValue().stripTrailingZeros());
        }
        return result;
    }
}
