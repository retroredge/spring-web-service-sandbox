package uk.co.redsoft.sandbox.domain.ports.in;

import uk.co.redsoft.sandbox.domain.model.PriceCatalogueStubResponse;

public interface PriceCatalogueStubUseCase {

    void prime(PriceCatalogueStubResponse response);
}
