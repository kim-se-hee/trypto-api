# portfolio 크로스 컨텍스트 인터페이스

패키지: `ksh.tryptobackend.portfolio.application.port.in`

## UseCase

### FindSnapshotsUseCase
- `findLatestByRoundIdAndExchangeId(Long roundId, Long exchangeId) → Optional<SnapshotInfoResult>`
- `findAllByRoundIdAndExchangeId(Long roundId, Long exchangeId) → List<SnapshotInfoResult>`

### FindSnapshotSummariesUseCase
- `findLatestSummaries(LocalDate snapshotDate) → List<SnapshotSummaryResult>`

### FindSnapshotDetailsUseCase
- `findLatestSnapshotDetails(Long userId, Long roundId) → List<SnapshotDetailResult>`

## Result DTO

| DTO | 필드 |
|-----|------|
| SnapshotInfoResult | snapshotId: Long, roundId: Long, exchangeId: Long, totalAsset: BigDecimal, totalInvestment: BigDecimal, totalProfitRate: BigDecimal, snapshotDate: LocalDate |
| SnapshotSummaryResult | userId: Long, roundId: Long, totalAssetKrw: BigDecimal, totalInvestmentKrw: BigDecimal |
| SnapshotDetailResult | coinId: Long, exchangeId: Long, assetRatio: BigDecimal, profitRate: BigDecimal |
