package ksh.tryptobackend.trading.application.port.in;

import ksh.tryptobackend.trading.application.port.in.dto.command.NotifyOrderFilledCommand;

public interface NotifyOrderFilledUseCase {

    void notifyOrderFilled(NotifyOrderFilledCommand command);
}
