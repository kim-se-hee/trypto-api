# 데이터 모델

## 도메인별 Aggregate 구조

| 도메인 | Aggregate Root | Entity | Value Object |
|--------|---------------|--------|--------------|
| Wallet | Wallet | WalletBalance, DepositAddress | DepositTargetExchange |
| Transfer | Transfer | — | TransferStatus, TransferType, TransferFailureReason, TransferBalanceChange, TransferDestination, TransferDestinationChain, TransferSourceExchange, WithdrawalCondition, TransferWallet, TransferDepositAddress |
| Trading | Order | Holding | Side, OrderType, OrderStatus, Fee, Quantity, BalanceChange, OrderAmountPolicy, TradingVenue, ListedCoinRef, RuleViolation, ViolationRule, ViolationCheckContext |
| MarketData | Exchange | ExchangeCoinChain, WithdrawalFee | ExchangeMarketType |
| Ranking | Ranking | PortfolioSnapshot, SnapshotDetail, EvaluatedHolding | ProfitRate, RankingPeriod, KrwConversionRate, RoundKey, RankingCandidate, EligibleRound, WalletSnapshot, ExchangeSnapshot, ActiveRound, SnapshotSummaries, RankingCandidates, EligibleRounds, EvaluatedHoldings |
| InvestmentRound | InvestmentRound | RuleSetting, EmergencyFunding | RoundStatus, SeedAmountPolicy, SeedAllocation, SeedAllocations |
| RegretAnalysis | RegretReport | RuleImpact, ViolationDetail, AssetSnapshot, TradeViolation | ViolationDetails, ImpactGap, ThresholdUnit, AssetTimeline, BtcBenchmark, BtcDailyPrice, CumulativeLossTimeline, ViolationMarkers, ViolationLossStrategy, ViolationLossContext, AnalysisRound, AnalysisRoundStatus, AnalysisRule, AnalysisRules, AnalysisExchange, AnalysisExchangeProfile, ActiveRoundExchange, TradeRecord, TradeSide, RuleViolation |
| Common (Shared Kernel) | — | — | RuleType |

**소유 관계:**
- Wallet → WalletBalance, DepositAddress
- Transfer → TransferBalanceChange, TransferDestination
- TradingVenue → OrderAmountPolicy
- Order → Side, OrderType, OrderStatus, Fee, Quantity
- ViolationRule → ViolationCheckContext, RuleViolation
- Exchange → ExchangeCoinChain, WithdrawalFee
- PortfolioSnapshot → SnapshotDetail
- EvaluatedHoldings → EvaluatedHolding
- RankingCandidates → RankingCandidate
- EligibleRounds → EligibleRound
- RuleSetting → RuleType
- SeedAllocations → SeedAllocation
- Ranking → RankingPeriod
- RuleImpact → ImpactGap
- RegretReport → RuleImpact, ViolationDetails
- ViolationDetails → ViolationDetail
- AssetTimeline → AssetSnapshot
- CumulativeLossTimeline → DailyLoss
- ViolationMarkers → ViolationMarker
- BtcBenchmark → BtcDailyPrice
- AnalysisRules → AnalysisRule
- TradeViolation → ViolationLossStrategy, ViolationLossContext

## 모듈 간 의존

| From → To | 참조 방식 | 용도 |
|-----------|----------|------|
| InvestmentRound → Wallet | FindWalletUseCase | InvestmentRound 1:N Wallet |
| Wallet → MarketData | exchangeId, coinId | 거래소-코인-체인 지원 확인 |
| Transfer → Wallet | walletId | 잔고 차감/추가/잠금, 입금 주소 역조회 |
| Transfer → MarketData | exchangeId, coinId | 수수료 조회, 체인 지원 확인 |
| Trading → Wallet | walletId | 잔고 검증, 잔고 반영 |
| Trading → MarketData | ListedCoinPort (FindExchangeCoinMappingUseCase), TradingVenuePort (FindExchangeDetailUseCase) | 거래소-코인 매핑 조회, 수수료율·주문금액정책 조회 |
| Trading → InvestmentRound | ViolationRulePort (FindInvestmentRulesUseCase + FindWalletUseCase) | walletId → roundId → 투자 원칙 위반 검증 |
| Ranking → MarketData | — | 현재가 조회 |
| Ranking → InvestmentRound | ActiveRoundQueryPort (FindActiveRoundsUseCase) | 활성 라운드 조회 |
| Ranking → InvestmentRound + Trading | EligibleRoundQueryPort (FindActiveRoundsUseCase + 거래 이력) | 랭킹 참여 자격 판단 |
| RegretAnalysis → Trading | TradeRecordPort | 원칙 위반 주문 체결 이력 조회 |
| RegretAnalysis → Trading | RuleViolationPort | 규칙 위반 기록 조회 |
| RegretAnalysis → MarketData | AnalysisExchangePort (FindExchangeDetailUseCase) | 거래소 정보 조회 |
| RegretAnalysis → InvestmentRound | AnalysisRoundPort (FindRoundInfoUseCase) | 라운드 정보 조회 |
| RegretAnalysis → InvestmentRound | AnalysisRulePort (FindInvestmentRulesUseCase) | 투자 원칙 조회 |
| RegretAnalysis → InvestmentRound + Wallet | ActiveRoundExchangePort (FindActiveRoundsUseCase + FindWalletUseCase) | 활성 라운드별 거래소 조회 |
| RegretAnalysis → Ranking | PortfolioSnapshotPort | 포트폴리오 스냅샷 조회 |
