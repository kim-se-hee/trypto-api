package ksh.tryptobackend.marketdata.application.port.out;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface LivePriceQueryPort {

    BigDecimal getCurrentPrice(Long exchangeCoinId);

    Map<Long, BigDecimal> getCurrentPrices(Set<Long> exchangeCoinIds);
}
