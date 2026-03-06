package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.investmentround.application.port.in.FindRoundInfoUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.RoundInfoResult;
import ksh.tryptobackend.regretanalysis.application.port.out.InvestmentRoundPort;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.AnalysisRoundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("regretInvestmentRoundAdapter")
@RequiredArgsConstructor
public class InvestmentRoundAdapter implements InvestmentRoundPort {

    private final FindRoundInfoUseCase findRoundInfoUseCase;

    @Override
    public ksh.tryptobackend.regretanalysis.application.port.out.dto.RoundInfoResult getRound(Long roundId) {
        RoundInfoResult result = findRoundInfoUseCase.findById(roundId)
            .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));

        return new ksh.tryptobackend.regretanalysis.application.port.out.dto.RoundInfoResult(
            result.roundId(), result.userId(), result.initialSeed(),
            toAnalysisRoundStatus(result.status()), result.startedAt(), result.endedAt()
        );
    }

    private AnalysisRoundStatus toAnalysisRoundStatus(String status) {
        return AnalysisRoundStatus.valueOf(status);
    }
}
