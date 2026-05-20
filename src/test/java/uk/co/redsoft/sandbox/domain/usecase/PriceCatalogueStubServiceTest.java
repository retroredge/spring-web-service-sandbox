package uk.co.redsoft.sandbox.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.PriceCatalogueStubResponse;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCatalogueStubRepositoryPort;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceCatalogueStubServiceTest {

    @Mock
    private PriceCatalogueStubRepositoryPort repositoryPort;

    @InjectMocks
    private PriceCatalogueStubService service;

    @Test
    void prime_delegatesToRepositoryPort() {
        var response = new PriceCatalogueStubResponse("978-0132350884", 200, "{\"status\":\"ok\"}");

        service.prime(response);

        verify(repositoryPort).save(response);
    }
}
