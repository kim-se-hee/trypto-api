package ksh.tryptobackend.regretanalysis.adapter.out;

import ksh.tryptobackend.trading.application.port.in.FindFilledOrdersUseCase;
import ksh.tryptobackend.trading.application.port.in.dto.result.FilledOrderResult;
import ksh.tryptobackend.regretanalysis.application.port.out.OrderExecutionQueryPort;
import ksh.tryptobackend.regretanalysis.domain.vo.OrderExecution;
import ksh.tryptobackend.regretanalysis.domain.vo.TradeSide;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderExecutionQueryAdapter implements OrderExecutionQueryPort {

    private final FindFilledOrdersUseCase findFilledOrdersUseCase;

    @Override
    public List<OrderExecution> findByOrderIds(List<Long> orderIds) {
        return findFilledOrdersUseCase.findByOrderIds(orderIds).stream()
            .map(this::toOrderExecution)
            .toList();
    }

    @Override
    public List<OrderExecution> findSellOrdersAfter(Long walletId, Long exchangeCoinId, LocalDateTime after) {
        return findFilledOrdersUseCase.findSellOrders(walletId, exchangeCoinId, after).stream()
            .map(this::toOrderExecution)
            .toList();
    }

    private OrderExecution toOrderExecution(FilledOrderResult result) {
        return new OrderExecution(
            result.orderId(), result.walletId(), result.exchangeCoinId(),
            TradeSide.valueOf(result.side()),
            result.amount(), result.quantity(), result.filledPrice(), result.filledAt()
        );
    }
}
