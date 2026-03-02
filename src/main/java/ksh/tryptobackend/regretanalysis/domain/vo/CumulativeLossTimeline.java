package ksh.tryptobackend.regretanalysis.domain.vo;

import ksh.tryptobackend.regretanalysis.domain.model.ViolationDetail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CumulativeLossTimeline {

    private final Map<LocalDate, BigDecimal> lossByDate;

    private CumulativeLossTimeline(Map<LocalDate, BigDecimal> lossByDate) {
        this.lossByDate = lossByDate;
    }

    public static CumulativeLossTimeline build(List<ViolationDetail> violations,
                                                List<LocalDate> snapshotDates) {
        List<ViolationDetail> sortedViolations = violations.stream()
            .sorted(Comparator.comparing(v -> v.getOccurredAt().toLocalDate()))
            .toList();

        Map<LocalDate, BigDecimal> cumulativeLossByDate = new HashMap<>();
        BigDecimal cumulativeLoss = BigDecimal.ZERO;
        int violationIndex = 0;

        for (LocalDate snapshotDate : snapshotDates) {
            while (violationIndex < sortedViolations.size()
                && !sortedViolations.get(violationIndex).getOccurredAt().toLocalDate().isAfter(snapshotDate)) {
                cumulativeLoss = cumulativeLoss.add(sortedViolations.get(violationIndex).getLossAmount());
                violationIndex++;
            }
            cumulativeLossByDate.put(snapshotDate, cumulativeLoss);
        }
        return new CumulativeLossTimeline(cumulativeLossByDate);
    }

    public BigDecimal getLossAt(LocalDate date) {
        return lossByDate.getOrDefault(date, BigDecimal.ZERO);
    }

    public BigDecimal calculateRuleFollowedAsset(BigDecimal actualAsset, LocalDate date) {
        return actualAsset.add(getLossAt(date));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CumulativeLossTimeline that)) return false;
        if (lossByDate.size() != that.lossByDate.size()) return false;
        for (Map.Entry<LocalDate, BigDecimal> entry : lossByDate.entrySet()) {
            BigDecimal otherValue = that.lossByDate.get(entry.getKey());
            if (otherValue == null || entry.getValue().compareTo(otherValue) != 0) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(lossByDate.size());
        for (Map.Entry<LocalDate, BigDecimal> entry : lossByDate.entrySet()) {
            result = 31 * result + Objects.hash(entry.getKey(), entry.getValue().stripTrailingZeros());
        }
        return result;
    }
}
