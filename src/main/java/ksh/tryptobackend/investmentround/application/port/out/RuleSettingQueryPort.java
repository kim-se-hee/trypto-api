package ksh.tryptobackend.investmentround.application.port.out;

import ksh.tryptobackend.investmentround.application.port.out.dto.InvestmentRuleInfo;

import java.util.List;

public interface RuleSettingQueryPort {

    List<InvestmentRuleInfo> findByRoundId(Long roundId);
}
