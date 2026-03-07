package ksh.tryptobackend.portfolio.application.port.out;

import ksh.tryptobackend.portfolio.domain.vo.ActiveRound;

import java.util.List;

public interface ActiveRoundQueryPort {

    List<ActiveRound> findAllActiveRounds();
}
