package ksh.tryptobackend.user.application.port.in;

import ksh.tryptobackend.user.application.port.in.dto.command.ChangePortfolioVisibilityCommand;
import ksh.tryptobackend.user.domain.model.User;

public interface ChangePortfolioVisibilityUseCase {

    User changePortfolioVisibility(ChangePortfolioVisibilityCommand command);
}
