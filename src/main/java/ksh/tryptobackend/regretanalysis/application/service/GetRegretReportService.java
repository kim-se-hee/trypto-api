package ksh.tryptobackend.regretanalysis.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.regretanalysis.application.port.in.GetRegretReportUseCase;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.query.GetRegretReportQuery;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.result.RegretReportResult;
import ksh.tryptobackend.regretanalysis.application.port.out.AnalysisExchangeQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.AnalysisRoundQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.AnalysisRuleQueryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.RegretReportQueryPort;
import ksh.tryptobackend.regretanalysis.domain.model.RegretReport;
import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisExchange;
import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisRound;
import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisRule;
import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetRegretReportService implements GetRegretReportUseCase {

    private final AnalysisRoundQueryPort analysisRoundQueryPort;
    private final RegretReportQueryPort regretReportQueryPort;
    private final AnalysisRuleQueryPort analysisRuleQueryPort;
    private final AnalysisExchangeQueryPort analysisExchangeQueryPort;

    @Override
    public RegretReportResult getRegretReport(GetRegretReportQuery query) {
        validateRoundOwner(query.roundId(), query.userId());
        validateWalletExistsForExchange(query.roundId(), query.exchangeId());
        AnalysisExchange exchange = analysisExchangeQueryPort.getExchangeInfo(query.exchangeId());
        AnalysisRules rules = analysisRuleQueryPort.findByRoundId(query.roundId());

        RegretReport report = regretReportQueryPort.getByRoundIdAndExchangeId(
            query.roundId(), query.exchangeId());

        return toResult(report, exchange, rules);
    }

    private void validateRoundOwner(Long roundId, Long userId) {
        AnalysisRound round = analysisRoundQueryPort.getRound(roundId);
        if (!round.userId().equals(userId)) {
            throw new CustomException(ErrorCode.ROUND_ACCESS_DENIED);
        }
    }

    private void validateWalletExistsForExchange(Long roundId, Long exchangeId) {
        if (!analysisExchangeQueryPort.existsWalletForExchange(roundId, exchangeId)) {
            throw new CustomException(ErrorCode.WALLET_NOT_FOUND);
        }
    }

    private RegretReportResult toResult(RegretReport report, AnalysisExchange exchange,
                                        AnalysisRules rules) {
        Map<Long, AnalysisRule> ruleMap = rules.toMap();
        Map<Long, String> coinSymbols = analysisExchangeQueryPort.findCoinSymbolsByIds(
            report.getViolationDetails().extractCoinIds());

        return RegretReportResult.from(report, exchange, ruleMap, coinSymbols);
    }
}
