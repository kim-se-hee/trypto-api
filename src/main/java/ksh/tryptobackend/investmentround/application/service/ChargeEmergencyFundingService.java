package ksh.tryptobackend.investmentround.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.investmentround.application.port.in.ChargeEmergencyFundingUseCase;
import ksh.tryptobackend.investmentround.application.port.in.dto.command.ChargeEmergencyFundingCommand;
import ksh.tryptobackend.investmentround.application.port.in.dto.result.ChargeEmergencyFundingResult;
import ksh.tryptobackend.investmentround.application.port.out.EmergencyFundingPersistencePort;
import ksh.tryptobackend.investmentround.application.port.out.ExchangeInfoPort;
import ksh.tryptobackend.investmentround.application.port.out.FundingWalletPort;
import ksh.tryptobackend.investmentround.application.port.out.InvestmentRoundPersistencePort;
import ksh.tryptobackend.investmentround.application.port.out.dto.ExchangeInfo;
import ksh.tryptobackend.investmentround.domain.model.EmergencyFunding;
import ksh.tryptobackend.investmentround.domain.model.InvestmentRound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChargeEmergencyFundingService implements ChargeEmergencyFundingUseCase {

    private final InvestmentRoundPersistencePort investmentRoundPersistencePort;
    private final EmergencyFundingPersistencePort emergencyFundingPersistencePort;
    private final ExchangeInfoPort exchangeInfoPort;
    private final FundingWalletPort fundingWalletPort;
    private final Clock clock;

    @Override
    @Transactional
    public ChargeEmergencyFundingResult chargeEmergencyFunding(ChargeEmergencyFundingCommand command) {
        InvestmentRound round = getRound(command.roundId());
        round.validateOwnedBy(command.userId());

        Optional<EmergencyFunding> existing = emergencyFundingPersistencePort
            .findByRoundIdAndIdempotencyKey(command.roundId(), command.idempotencyKey());
        if (existing.isPresent()) {
        return toResult(existing.get(), round);
        }

        InvestmentRound updatedRound = round.chargeEmergencyFunding(command.amount());

        Long walletId = getWalletId(command.roundId(), command.exchangeId());
        ExchangeInfo exchange = getExchange(command.exchangeId());

        LocalDateTime now = LocalDateTime.now(clock);
        fundingWalletPort.addBalance(walletId, exchange.baseCurrencyCoinId(), command.amount());
        EmergencyFunding funding = saveEmergencyFunding(command, now);
        investmentRoundPersistencePort.save(updatedRound);

        return toResult(funding, updatedRound);
    }

    private InvestmentRound getRound(Long roundId) {
        return investmentRoundPersistencePort.findById(roundId)
            .orElseThrow(() -> new CustomException(ErrorCode.ROUND_NOT_FOUND));
    }

    private Long getWalletId(Long roundId, Long exchangeId) {
        return fundingWalletPort.findWalletId(roundId, exchangeId)
            .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
    }

    private ExchangeInfo getExchange(Long exchangeId) {
        return exchangeInfoPort.findById(exchangeId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));
    }

    private EmergencyFunding saveEmergencyFunding(ChargeEmergencyFundingCommand command, LocalDateTime now) {
        EmergencyFunding funding = EmergencyFunding.create(
            command.roundId(), command.exchangeId(), command.amount(), command.idempotencyKey(), now);
        return emergencyFundingPersistencePort.save(funding);
    }

    private ChargeEmergencyFundingResult toResult(EmergencyFunding funding, InvestmentRound round) {
        return new ChargeEmergencyFundingResult(
            round.getRoundId(),
            funding.getExchangeId(),
            funding.getAmount(),
            round.getEmergencyChargeCount(),
            funding.getCreatedAt()
        );
    }
}
