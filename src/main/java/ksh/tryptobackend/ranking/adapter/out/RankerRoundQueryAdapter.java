package ksh.tryptobackend.ranking.adapter.out;

import ksh.tryptobackend.investmentround.application.port.in.FindRoundInfoUseCase;
import ksh.tryptobackend.ranking.application.port.out.RankerRoundQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RankerRoundQueryAdapter implements RankerRoundQueryPort {

    private final FindRoundInfoUseCase findRoundInfoUseCase;

    @Override
    public Optional<Long> findActiveRoundIdByUserId(Long userId) {
        return findRoundInfoUseCase.findActiveByUserId(userId)
            .map(result -> result.roundId());
    }
}
