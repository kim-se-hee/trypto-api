package ksh.tryptobackend.wallet.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.wallet.application.port.in.IssueDepositAddressUseCase;
import ksh.tryptobackend.wallet.application.port.in.dto.command.IssueDepositAddressCommand;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangeCoinChainPort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressExchangePort;
import ksh.tryptobackend.wallet.application.port.out.DepositAddressPersistencePort;
import ksh.tryptobackend.wallet.application.port.out.WalletQueryPort;
import ksh.tryptobackend.wallet.application.port.out.dto.DepositAddressChainInfo;
import ksh.tryptobackend.wallet.application.port.out.dto.WalletInfo;
import ksh.tryptobackend.wallet.domain.model.DepositAddress;
import ksh.tryptobackend.wallet.domain.vo.DepositTargetExchange;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class IssueDepositAddressService implements IssueDepositAddressUseCase {

    private final WalletQueryPort walletQueryPort;
    private final DepositAddressExchangePort exchangePort;
    private final DepositAddressExchangeCoinChainPort chainPort;
    private final DepositAddressPersistencePort depositAddressPersistencePort;
    private final TransactionTemplate transactionTemplate;

    @Override
    public DepositAddress issueDepositAddress(IssueDepositAddressCommand command) {
        WalletInfo wallet = getWallet(command.walletId());
        validateTransferable(wallet.exchangeId(), command.coinId());

        return depositAddressPersistencePort.findByWalletIdAndChain(command.walletId(), command.chain())
            .orElseGet(() -> createDepositAddress(wallet.exchangeId(), command));
    }

    private void validateTransferable(Long exchangeId, Long coinId) {
        DepositTargetExchange exchange = exchangePort.getExchange(exchangeId);
        exchange.validateTransferable(coinId);
    }

    private DepositAddress createDepositAddress(Long exchangeId, IssueDepositAddressCommand command) {
        DepositAddressChainInfo chainInfo = chainPort.getExchangeCoinChain(
            exchangeId, command.coinId(), command.chain());

        try {
            return transactionTemplate.execute(status ->
                depositAddressPersistencePort.save(
                    DepositAddress.create(command.walletId(), command.chain(), chainInfo.tagRequired())));
        } catch (DataIntegrityViolationException e) {
            return depositAddressPersistencePort.findByWalletIdAndChain(command.walletId(), command.chain())
                .orElseThrow(() -> new CustomException(ErrorCode.CONCURRENT_MODIFICATION));
        }
    }

    private WalletInfo getWallet(Long walletId) {
        return walletQueryPort.findById(walletId)
            .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
    }
}
