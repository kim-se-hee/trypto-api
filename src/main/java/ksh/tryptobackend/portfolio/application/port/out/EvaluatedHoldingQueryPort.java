package ksh.tryptobackend.portfolio.application.port.out;

import ksh.tryptobackend.portfolio.domain.model.EvaluatedHoldings;

public interface EvaluatedHoldingQueryPort {

    EvaluatedHoldings findAllByWalletId(Long walletId, Long exchangeId);
}
