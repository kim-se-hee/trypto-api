package ksh.tryptobackend.regretanalysis.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.regretanalysis.application.port.in.GetRegretChartUseCase;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.query.GetRegretChartQuery;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.result.RegretChartResult;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.result.RegretChartResult.ChartDataPoint;
import ksh.tryptobackend.regretanalysis.application.port.in.dto.result.RegretChartResult.ViolationMarkerPoint;
import ksh.tryptobackend.regretanalysis.application.port.out.BtcPriceHistoryPort;
import ksh.tryptobackend.regretanalysis.application.port.out.ExchangeInfoPort;
import ksh.tryptobackend.regretanalysis.application.port.out.InvestmentRoundPort;
import ksh.tryptobackend.regretanalysis.application.port.out.PortfolioSnapshotPort;
import ksh.tryptobackend.regretanalysis.application.port.out.RegretReportPersistencePort;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.AssetSnapshot;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.BtcDailyPrice;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.ExchangeInfoRecord;
import ksh.tryptobackend.regretanalysis.application.port.out.dto.RoundInfoResult;
import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRegretChartService implements GetRegretChartUseCase {

    private static final int PRICE_SCALE = 8;
    private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

    private final InvestmentRoundPort investmentRoundPort;
    private final RegretReportPersistencePort regretReportPersistencePort;
    private final PortfolioSnapshotPort portfolioSnapshotPort;
    private final BtcPriceHistoryPort btcPriceHistoryPort;
    private final ExchangeInfoPort exchangeInfoPort;

    @Override
    @Transactional(readOnly = true)
    public RegretChartResult getRegretChart(GetRegretChartQuery query) {
        RoundInfoResult round = getRoundAndValidateOwner(query);
        validateReportExists(query);
        List<ViolationDetail> violations = getViolationDetails(query);
        ExchangeInfoRecord exchangeInfo = getExchangeInfo(query.exchangeId());
        List<AssetSnapshot> snapshots = getSnapshots(query);

        List<ChartDataPoint> assetHistory = buildAssetHistory(snapshots, violations, exchangeInfo.currency());
        List<ViolationMarkerPoint> violationMarkers = buildViolationMarkers(violations, snapshots);
        int totalDays = calculateTotalDays(snapshots);

        return new RegretChartResult(
            query.roundId(), query.exchangeId(),
            exchangeInfo.name(), exchangeInfo.currency(),
            totalDays, assetHistory, violationMarkers
        );
    }

    private RoundInfoResult getRoundAndValidateOwner(GetRegretChartQuery query) {
        RoundInfoResult round = investmentRoundPort.getRound(query.roundId());
        if (!round.userId().equals(query.userId())) {
            throw new CustomException(ErrorCode.ROUND_ACCESS_DENIED);
        }
        return round;
    }

    private void validateReportExists(GetRegretChartQuery query) {
        if (!regretReportPersistencePort.existsByRoundIdAndExchangeId(query.roundId(), query.exchangeId())) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }
    }

    private List<ViolationDetail> getViolationDetails(GetRegretChartQuery query) {
        return regretReportPersistencePort.findViolationDetailsByRoundIdAndExchangeId(
            query.roundId(), query.exchangeId());
    }

    private ExchangeInfoRecord getExchangeInfo(Long exchangeId) {
        return exchangeInfoPort.getExchangeInfo(exchangeId);
    }

    private List<AssetSnapshot> getSnapshots(GetRegretChartQuery query) {
        List<AssetSnapshot> snapshots = portfolioSnapshotPort.findAllByRoundIdAndExchangeId(
            query.roundId(), query.exchangeId());
        if (snapshots.isEmpty()) {
            throw new CustomException(ErrorCode.SNAPSHOT_NOT_FOUND);
        }
        return snapshots;
    }

    private List<ChartDataPoint> buildAssetHistory(List<AssetSnapshot> snapshots,
                                                      List<ViolationDetail> violations,
                                                      String currency) {
        Map<LocalDate, BigDecimal> cumulativeLossMap = buildCumulativeLossMap(violations, snapshots);
        Map<LocalDate, BigDecimal> btcHoldAssetMap = buildBtcHoldAssetMap(snapshots, currency);

        return snapshots.stream()
            .map(snapshot -> {
                LocalDate date = snapshot.snapshotDate().toLocalDate();
                BigDecimal actualAsset = snapshot.totalAsset();
                BigDecimal cumulativeLoss = cumulativeLossMap.getOrDefault(date, BigDecimal.ZERO);
                BigDecimal ruleFollowedAsset = actualAsset.add(cumulativeLoss);
                BigDecimal btcHoldAsset = btcHoldAssetMap.getOrDefault(date, BigDecimal.ZERO);
                return new ChartDataPoint(date, actualAsset, ruleFollowedAsset, btcHoldAsset);
            })
            .toList();
    }

    private Map<LocalDate, BigDecimal> buildCumulativeLossMap(List<ViolationDetail> violations,
                                                               List<AssetSnapshot> snapshots) {
        List<ViolationDetail> sortedViolations = violations.stream()
            .sorted(Comparator.comparing(v -> v.getOccurredAt().toLocalDate()))
            .toList();

        Map<LocalDate, BigDecimal> cumulativeLossByDate = new HashMap<>();
        BigDecimal cumulativeLoss = BigDecimal.ZERO;
        int violationIndex = 0;

        for (AssetSnapshot snapshot : snapshots) {
            LocalDate snapshotDate = snapshot.snapshotDate().toLocalDate();
            while (violationIndex < sortedViolations.size()
                && !sortedViolations.get(violationIndex).getOccurredAt().toLocalDate().isAfter(snapshotDate)) {
                cumulativeLoss = cumulativeLoss.add(sortedViolations.get(violationIndex).getLossAmount());
                violationIndex++;
            }
            cumulativeLossByDate.put(snapshotDate, cumulativeLoss);
        }
        return cumulativeLossByDate;
    }

    private Map<LocalDate, BigDecimal> buildBtcHoldAssetMap(List<AssetSnapshot> snapshots,
                                                             String currency) {
        LocalDate startDate = snapshots.getFirst().snapshotDate().toLocalDate();
        LocalDate endDate = snapshots.getLast().snapshotDate().toLocalDate();

        List<BtcDailyPrice> btcPrices = btcPriceHistoryPort.findBtcDailyPrices(startDate, endDate, currency);
        Map<LocalDate, BigDecimal> priceMap = btcPrices.stream()
            .collect(Collectors.toMap(BtcDailyPrice::date, BtcDailyPrice::closePrice));

        BigDecimal seedMoney = snapshots.getFirst().totalAsset();
        BigDecimal btcPriceAtStart = priceMap.get(startDate);

        if (btcPriceAtStart == null || btcPriceAtStart.compareTo(BigDecimal.ZERO) == 0) {
            return Map.of();
        }

        BigDecimal btcQuantity = seedMoney.divide(btcPriceAtStart, PRICE_SCALE, RoundingMode.HALF_UP);

        return snapshots.stream()
            .collect(Collectors.toMap(
                snapshot -> snapshot.snapshotDate().toLocalDate(),
                snapshot -> {
                    BigDecimal dailyPrice = priceMap.get(snapshot.snapshotDate().toLocalDate());
                    if (dailyPrice == null) {
                        return BigDecimal.ZERO;
                    }
                    return btcQuantity.multiply(dailyPrice, MATH_CONTEXT);
                }
            ));
    }

    private List<ViolationMarkerPoint> buildViolationMarkers(List<ViolationDetail> violations,
                                                               List<AssetSnapshot> snapshots) {
        Map<LocalDate, BigDecimal> assetByDate = snapshots.stream()
            .collect(Collectors.toMap(
                s -> s.snapshotDate().toLocalDate(),
                AssetSnapshot::totalAsset
            ));

        Set<LocalDate> violationDates = violations.stream()
            .map(v -> v.getOccurredAt().toLocalDate())
            .collect(Collectors.toSet());

        return violationDates.stream()
            .sorted()
            .filter(assetByDate::containsKey)
            .map(date -> new ViolationMarkerPoint(date, assetByDate.get(date)))
            .toList();
    }

    private int calculateTotalDays(List<AssetSnapshot> snapshots) {
        LocalDate first = snapshots.getFirst().snapshotDate().toLocalDate();
        LocalDate last = snapshots.getLast().snapshotDate().toLocalDate();
        return (int) ChronoUnit.DAYS.between(first, last) + 1;
    }
}
