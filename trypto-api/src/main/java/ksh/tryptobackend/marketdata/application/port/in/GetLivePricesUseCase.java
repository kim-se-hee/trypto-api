package ksh.tryptobackend.marketdata.application.port.in;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface GetLivePricesUseCase {

    Map<Long, BigDecimal> getCurrentPrices(Set<Long> exchangeCoinIds);
}
