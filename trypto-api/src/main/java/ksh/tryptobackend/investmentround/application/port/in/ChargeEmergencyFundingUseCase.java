package ksh.tryptobackend.investmentround.application.port.in;

import ksh.tryptobackend.investmentround.application.port.in.dto.command.ChargeEmergencyFundingCommand;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.ChargeEmergencyFundingResult;

public interface ChargeEmergencyFundingUseCase {

    ChargeEmergencyFundingResult chargeEmergencyFunding(ChargeEmergencyFundingCommand command);
}
