package ksh.tryptobackend.trading.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class QuantityCalculator {

    private static final int QUANTITY_SCALE = 8;

    private QuantityCalculator() {
    }

    public static BigDecimal calculate(BigDecimal orderAmount, BigDecimal price) {
        return orderAmount.divide(price, QUANTITY_SCALE, RoundingMode.FLOOR);
    }
}
