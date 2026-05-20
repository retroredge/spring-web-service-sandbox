package uk.co.redsoft.sandbox.adapters.out.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.PriceCatalogueStubResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceCatalogueStubRepositoryAdapterTest {

    @Mock
    private JpaPriceCatalogueStubRepository jpaRepository;

    @InjectMocks
    private PriceCatalogueStubRepositoryAdapter adapter;

    @Test
    void findByIsbn_delegatesToJpaRepoAndMapsToDomain() {
        var isbn = "978-0132350884";
        when(jpaRepository.findByIsbn(isbn))
                .thenReturn(Optional.of(new PriceCatalogueStubResponseEntity(1L, isbn, 200, "{}")));

        var result = adapter.findByIsbn(isbn);

        assertThat(result).isPresent();
        assertThat(result.get().isbn()).isEqualTo(isbn);
        assertThat(result.get().httpStatus()).isEqualTo(200);
        assertThat(result.get().responseBody()).isEqualTo("{}");
    }

    @Test
    void findByIsbn_returnsEmptyWhenNotFound() {
        when(jpaRepository.findByIsbn("978-0000000000")).thenReturn(Optional.empty());

        var result = adapter.findByIsbn("978-0000000000");

        assertThat(result).isEmpty();
    }

    @Test
    void save_insertsNewRecordWhenIsbnNotPresent() {
        var isbn = "978-0132350884";
        when(jpaRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        adapter.save(new PriceCatalogueStubResponse(isbn, 200, "{}"));

        verify(jpaRepository).save(any(PriceCatalogueStubResponseEntity.class));
    }

    @Test
    void save_updatesExistingRecordWhenIsbnAlreadyPresent() {
        var isbn = "978-0132350884";
        when(jpaRepository.findByIsbn(isbn))
                .thenReturn(Optional.of(new PriceCatalogueStubResponseEntity(42L, isbn, 200, "{\"old\":true}")));

        adapter.save(new PriceCatalogueStubResponse(isbn, 404, ""));

        verify(jpaRepository).save(any(PriceCatalogueStubResponseEntity.class));
    }
}
