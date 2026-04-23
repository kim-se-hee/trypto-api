package ksh.tryptobackend.investmentround.application.port.in;

import java.math.BigDecimal;

public interface SumEmergencyFundingUseCase {

    BigDecimal sumByRoundId(Long roundId);

    BigDecimal sumByRoundIdAndExchangeId(Long roundId, Long exchangeId);
}
