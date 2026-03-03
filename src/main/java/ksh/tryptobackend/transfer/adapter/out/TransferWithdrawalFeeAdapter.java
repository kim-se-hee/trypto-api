package ksh.tryptobackend.transfer.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.application.port.out.WithdrawalFeeQueryPort;
import ksh.tryptobackend.transfer.application.port.out.TransferWithdrawalFeePort;
import ksh.tryptobackend.transfer.application.port.out.dto.TransferWithdrawalFeeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferWithdrawalFeeAdapter implements TransferWithdrawalFeePort {

    private final WithdrawalFeeQueryPort withdrawalFeeQueryPort;

    @Override
    public TransferWithdrawalFeeInfo getWithdrawalFee(Long exchangeId, Long coinId, String chain) {
        return withdrawalFeeQueryPort.findByExchangeIdAndCoinIdAndChain(exchangeId, coinId, chain)
            .map(info -> new TransferWithdrawalFeeInfo(info.fee(), info.minWithdrawal()))
            .orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_CHAIN));
    }
}
