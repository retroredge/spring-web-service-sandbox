package uk.co.redsoft.sandbox.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPriceCatalogueStubRepository extends JpaRepository<PriceCatalogueStubResponseEntity, Long> {

    Optional<PriceCatalogueStubResponseEntity> findByIsbn(String isbn);
}
