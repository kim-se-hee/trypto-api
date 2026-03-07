package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.domain.vo.AnalysisExchange;

import java.util.Map;
import java.util.Set;

public interface AnalysisExchangeQueryPort {

    AnalysisExchange getExchangeInfo(Long exchangeId);

    boolean existsWalletForExchange(Long roundId, Long exchangeId);

    Map<Long, String> findCoinSymbolsByIds(Set<Long> coinIds);
}
