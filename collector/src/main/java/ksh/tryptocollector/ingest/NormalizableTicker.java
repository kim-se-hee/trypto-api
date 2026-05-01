package ksh.tryptocollector.ingest;

import ksh.tryptocollector.model.NormalizedTicker;

public interface NormalizableTicker {
    String code();

    NormalizedTicker toNormalized(String displayName);
}
