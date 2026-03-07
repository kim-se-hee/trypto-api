package ksh.tryptobackend.regretanalysis.application.port.out;

import ksh.tryptobackend.regretanalysis.domain.vo.OrderExecution;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderExecutionQueryPort {

    List<OrderExecution> findByOrderIds(List<Long> orderIds);

    List<OrderExecution> findSellOrdersAfter(Long walletId, Long exchangeCoinId, LocalDateTime after);
}
