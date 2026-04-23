package ksh.tryptobackend.regretanalysis.application.port.in;

import ksh.tryptobackend.regretanalysis.application.port.in.dto.query.GetRegretChartQuery;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.result.RegretChartResult;

public interface GetRegretChartUseCase {

    RegretChartResult getRegretChart(GetRegretChartQuery query);
}
