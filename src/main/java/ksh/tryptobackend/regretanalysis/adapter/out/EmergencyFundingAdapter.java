package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.SumEmergencyFundingUseCase;
import ksh.tryptobackend.regretanalysis.application.port.out.EmergencyFundingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("regretEmergencyFundingAdapter")
@RequiredArgsConstructor
public class EmergencyFundingAdapter implements EmergencyFundingPort {

    private final SumEmergencyFundingUseCase sumEmergencyFundingUseCase;

    @Override
    public BigDecimal getTotalFundingAmount(Long roundId) {
        return sumEmergencyFundingUseCase.sumByRoundId(roundId);
    }
}
