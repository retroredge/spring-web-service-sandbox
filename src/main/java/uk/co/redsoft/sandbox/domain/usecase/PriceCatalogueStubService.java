package uk.co.redsoft.sandbox.domain.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.co.redsoft.sandbox.domain.model.PriceCatalogueStubResponse;
import uk.co.redsoft.sandbox.domain.ports.in.PriceCatalogueStubUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCatalogueStubRepositoryPort;

@RequiredArgsConstructor
@Service
public class PriceCatalogueStubService implements PriceCatalogueStubUseCase {

    private final PriceCatalogueStubRepositoryPort repositoryPort;

    @Override
    public void prime(PriceCatalogueStubResponse response) {
        repositoryPort.save(response);
    }
}
