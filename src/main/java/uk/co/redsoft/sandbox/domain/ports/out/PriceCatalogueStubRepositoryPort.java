package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.PriceCatalogueStubResponse;

import java.util.Optional;

public interface PriceCatalogueStubRepositoryPort {

    Optional<PriceCatalogueStubResponse> findByIsbn(String isbn);

    void save(PriceCatalogueStubResponse response);
}
