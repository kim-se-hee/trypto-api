package ksh.tryptobackend.trading.domain.vo;

import java.util.Map;
import java.util.Objects;

public class CoinExchangeMapping {

    private final Map<Long, Long> exchangeCoinIdByCoinId;

    public CoinExchangeMapping(Map<Long, Long> exchangeCoinIdByCoinId) {
        this.exchangeCoinIdByCoinId = Map.copyOf(exchangeCoinIdByCoinId);
    }

    public Long getExchangeCoinId(Long coinId) {
        return exchangeCoinIdByCoinId.get(coinId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoinExchangeMapping that = (CoinExchangeMapping) o;
        return Objects.equals(exchangeCoinIdByCoinId, that.exchangeCoinIdByCoinId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangeCoinIdByCoinId);
    }
}
