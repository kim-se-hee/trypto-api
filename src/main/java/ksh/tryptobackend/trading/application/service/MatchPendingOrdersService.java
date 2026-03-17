package ksh.tryptobackend.trading.application.service;

import ksh.tryptobackend.marketdata.application.port.in.FindExchangeCoinMappingUseCase;
import ksh.tryptobackend.marketdata.application.port.in.FindExchangeDetailUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.ExchangeCoinMappingResult;
import ksh.tryptobackend.trading.application.port.out.ExchangeCoinMappingCachePort;
import ksh.tryptobackend.trading.application.port.out.HoldingCommandPort;
import ksh.tryptobackend.trading.application.port.out.OrderCommandPort;
import ksh.tryptobackend.trading.application.port.out.OrderFillFailureCommandPort;
import ksh.tryptobackend.trading.application.port.out.PendingOrderCacheCommandPort;
import ksh.tryptobackend.trading.domain.model.Holding;
import ksh.tryptobackend.trading.domain.model.Order;
import ksh.tryptobackend.trading.domain.model.OrderFillFailure;
import ksh.tryptobackend.trading.domain.vo.OrderFilledEvent;
import ksh.tryptobackend.trading.domain.vo.PendingOrder;
import ksh.tryptobackend.trading.domain.vo.TradingVenue;
import ksh.tryptobackend.wallet.application.port.in.GetWalletOwnerIdUseCase;
import ksh.tryptobackend.wallet.application.port.in.ManageWalletBalanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchPendingOrdersService {

    private static final int MAX_RETRY_COUNT = 2;
    private static final long[] RETRY_BACKOFF_MS = {50, 100};

    private final OrderCommandPort orderCommandPort;
    private final HoldingCommandPort holdingCommandPort;
    private final PendingOrderCacheCommandPort pendingOrderCacheCommandPort;
    private final ExchangeCoinMappingCachePort exchangeCoinMappingCachePort;
    private final OrderFillFailureCommandPort orderFillFailureCommandPort;

    private final FindExchangeCoinMappingUseCase findExchangeCoinMappingUseCase;
    private final FindExchangeDetailUseCase findExchangeDetailUseCase;

    private final ManageWalletBalanceUseCase manageWalletBalanceUseCase;
    private final GetWalletOwnerIdUseCase getWalletOwnerIdUseCase;

    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public void matchOrders(String exchange, String symbol, BigDecimal currentPrice) {
        Long exchangeCoinId = resolveExchangeCoinId(exchange, symbol);
        if (exchangeCoinId == null) {
            return;
        }

        List<PendingOrder> matchedOrders = pendingOrderCacheCommandPort
            .findMatchedOrders(exchangeCoinId, currentPrice);
        if (matchedOrders.isEmpty()) {
            return;
        }

        for (PendingOrder pendingOrder : matchedOrders) {
            pendingOrderCacheCommandPort.remove(exchangeCoinId, pendingOrder.orderId());
            processFillWithRetry(pendingOrder, currentPrice);
        }
    }

    @Transactional
    public void fillOrder(Long orderId, BigDecimal currentPrice) {
        Order order = orderCommandPort.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("주문 조회 실패 (삭제됨): orderId={}", orderId);
            return;
        }

        if (!order.isPending()) {
            log.info("주문이 이미 처리됨: orderId={}, status={}", orderId, order.getStatus());
            return;
        }

        order.fill(LocalDateTime.now(clock));

        ExchangeCoinMappingResult mapping = findExchangeCoinMapping(order.getExchangeCoinId());
        TradingVenue venue = getTradingVenue(mapping.exchangeId());

        settleBalance(order, mapping, venue);
        updateHolding(order, mapping, currentPrice);

        orderCommandPort.save(order);

        publishOrderFilledEvent(order, mapping);
    }

    private Long resolveExchangeCoinId(String exchange, String symbol) {
        return exchangeCoinMappingCachePort.resolve(exchange, symbol)
            .orElseGet(() -> {
                log.warn("매핑 변환 실패: exchange={}, symbol={}", exchange, symbol);
                return null;
            });
    }

    private void processFillWithRetry(PendingOrder pendingOrder, BigDecimal currentPrice) {
        for (int attempt = 0; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                fillOrder(pendingOrder.orderId(), currentPrice);
                return;
            } catch (OptimisticLockingFailureException e) {
                log.info("낙관적 락 충돌 (취소/다른 서버 매칭): orderId={}", pendingOrder.orderId());
                return;
            } catch (Exception e) {
                if (attempt < MAX_RETRY_COUNT) {
                    sleepForRetry(attempt);
                    log.warn("체결 처리 재시도 ({}/{}): orderId={}", attempt + 1, MAX_RETRY_COUNT, pendingOrder.orderId());
                } else {
                    handleFillFailure(pendingOrder, currentPrice, e);
                }
            }
        }
    }

    private void sleepForRetry(int attempt) {
        try {
            Thread.sleep(RETRY_BACKOFF_MS[attempt]);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleFillFailure(PendingOrder pendingOrder, BigDecimal currentPrice, Exception e) {
        log.error("체결 처리 실패 (재시도 소진): orderId={}", pendingOrder.orderId(), e);
        pendingOrderCacheCommandPort.add(pendingOrder);
        recordFillFailure(pendingOrder, currentPrice, e);
    }

    private void recordFillFailure(PendingOrder pendingOrder, BigDecimal attemptedPrice, Exception e) {
        try {
            OrderFillFailure failure = OrderFillFailure.create(
                pendingOrder.orderId(), attemptedPrice,
                LocalDateTime.now(clock), e.getMessage());
            orderFillFailureCommandPort.save(failure);
        } catch (Exception saveEx) {
            log.error("체결 실패 이력 저장 실패: orderId={}", pendingOrder.orderId(), saveEx);
        }
    }

    private ExchangeCoinMappingResult findExchangeCoinMapping(Long exchangeCoinId) {
        return findExchangeCoinMappingUseCase.findById(exchangeCoinId)
            .orElseThrow(() -> new IllegalStateException("매핑 없음: exchangeCoinId=" + exchangeCoinId));
    }

    private TradingVenue getTradingVenue(Long exchangeId) {
        return findExchangeDetailUseCase.findExchangeDetail(exchangeId)
            .map(detail -> TradingVenue.of(detail.feeRate(), detail.baseCurrencyCoinId(), detail.domestic()))
            .orElseThrow(() -> new IllegalStateException("거래소 없음: exchangeId=" + exchangeId));
    }

    private void settleBalance(Order order, ExchangeCoinMappingResult mapping, TradingVenue venue) {
        if (order.isBuyOrder()) {
            settleBuyOrder(order, mapping, venue);
        } else {
            settleSellOrder(order, mapping, venue);
        }
    }

    private void settleBuyOrder(Order order, ExchangeCoinMappingResult mapping, TradingVenue venue) {
        Long baseCoinId = venue.baseCurrencyCoinId();
        BigDecimal unlockAmount = order.getSettlementDebit();

        manageWalletBalanceUseCase.unlockBalance(order.getWalletId(), baseCoinId, unlockAmount);
        manageWalletBalanceUseCase.deductBalance(order.getWalletId(), baseCoinId, unlockAmount);
        manageWalletBalanceUseCase.addBalance(order.getWalletId(), mapping.coinId(), order.getQuantity().value());
    }

    private void settleSellOrder(Order order, ExchangeCoinMappingResult mapping, TradingVenue venue) {
        BigDecimal unlockQuantity = order.getQuantity().value();

        manageWalletBalanceUseCase.unlockBalance(order.getWalletId(), mapping.coinId(), unlockQuantity);
        manageWalletBalanceUseCase.deductBalance(order.getWalletId(), mapping.coinId(), unlockQuantity);
        manageWalletBalanceUseCase.addBalance(order.getWalletId(), venue.baseCurrencyCoinId(), order.getSettlementCredit());
    }

    private void updateHolding(Order order, ExchangeCoinMappingResult mapping, BigDecimal currentPrice) {
        Holding holding = holdingCommandPort.findByWalletIdAndCoinId(order.getWalletId(), mapping.coinId())
            .orElseGet(() -> Holding.empty(order.getWalletId(), mapping.coinId()));
        holding.applyOrder(order.getSide(), order.getFilledPrice(), order.getQuantity().value(), currentPrice);
        holdingCommandPort.save(holding);
    }

    private void publishOrderFilledEvent(Order order, ExchangeCoinMappingResult mapping) {
        try {
            Long userId = getWalletOwnerIdUseCase.getWalletOwnerId(order.getWalletId());
            OrderFilledEvent event = OrderFilledEvent.from(
                order.getWalletId(), order.getId(), mapping.coinId(),
                order.getSide(), order.getQuantity().value(),
                order.getFilledPrice(), order.getFee().amount());
            eventPublisher.publishEvent(new OrderFilledNotification(userId, event));
        } catch (Exception e) {
            log.warn("체결 이벤트 발행 준비 실패: orderId={}", order.getId(), e);
        }
    }

    public record OrderFilledNotification(Long userId, OrderFilledEvent event) {
    }
}
