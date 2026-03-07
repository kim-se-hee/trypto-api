package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.domain.model.ViolatedOrder;

import java.util.List;

public interface ViolatedOrderQueryPort {

    List<ViolatedOrder> findByRoundIdAndExchangeId(Long roundId, Long exchangeId);
}
