package ksh.tryptobackend.batch.snapshot;

import ksh.tryptobackend.ranking.application.port.out.SnapshotPersistencePort;
import ksh.tryptobackend.ranking.domain.model.PortfolioSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class SnapshotItemWriter implements ItemWriter<SnapshotOutput> {

    private final SnapshotPersistencePort snapshotPersistencePort;

    @Override
    public void write(Chunk<? extends SnapshotOutput> chunk) {
        for (SnapshotOutput output : chunk) {
            PortfolioSnapshot saved = snapshotPersistencePort.save(output.snapshot());
            snapshotPersistencePort.saveDetails(saved.getId(), output.details());
        }
    }
}
