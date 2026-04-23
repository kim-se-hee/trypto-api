package ksh.tryptobackend.ranking.domain.vo;

import java.util.Map;
import java.util.Objects;

public class CoinSymbols {

    private final Map<Long, String> symbolByCoinId;

    public CoinSymbols(Map<Long, String> symbolByCoinId) {
        this.symbolByCoinId = Map.copyOf(symbolByCoinId);
    }

    public String getSymbol(Long coinId) {
        return symbolByCoinId.getOrDefault(coinId, "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoinSymbols that = (CoinSymbols) o;
        return Objects.equals(symbolByCoinId, that.symbolByCoinId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbolByCoinId);
    }
}
