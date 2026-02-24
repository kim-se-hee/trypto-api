package ksh.tryptobackend.trading.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Quantity(BigDecimal value) {

    private static final int SCALE = 8;

    public Quantity {
        value = value.setScale(SCALE, RoundingMode.FLOOR);
    }

    public static Quantity fromAmountAndPrice(BigDecimal amount, BigDecimal price) {
        BigDecimal result = amount.divide(price, SCALE, RoundingMode.FLOOR);
        return new Quantity(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity quantity)) return false;
        return value.compareTo(quantity.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }
}
