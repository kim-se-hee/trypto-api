package ksh.tryptobackend.portfolio.application.port.out;

import java.math.BigDecimal;

public interface EmergencyFundingSnapshotQueryPort {

    BigDecimal sumByRoundIdAndExchangeId(Long roundId, Long exchangeId);
}
