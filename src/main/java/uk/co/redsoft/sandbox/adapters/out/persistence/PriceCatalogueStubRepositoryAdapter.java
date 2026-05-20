package uk.co.redsoft.sandbox.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.domain.model.PriceCatalogueStubResponse;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCatalogueStubRepositoryPort;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PriceCatalogueStubRepositoryAdapter implements PriceCatalogueStubRepositoryPort {

    private final JpaPriceCatalogueStubRepository jpaRepository;

    @Override
    public Optional<PriceCatalogueStubResponse> findByIsbn(String isbn) {
        return jpaRepository.findByIsbn(isbn)
                .map(e -> new PriceCatalogueStubResponse(e.getIsbn(), e.getHttpStatus(), e.getResponseBody()));
    }

    @Override
    public void save(PriceCatalogueStubResponse response) {
        var existing = jpaRepository.findByIsbn(response.isbn());
        var entity = existing.isPresent()
                ? new PriceCatalogueStubResponseEntity(existing.get().getId(), response.isbn(), response.httpStatus(), response.responseBody())
                : new PriceCatalogueStubResponseEntity(response.isbn(), response.httpStatus(), response.responseBody());
        jpaRepository.save(entity);
    }
}
