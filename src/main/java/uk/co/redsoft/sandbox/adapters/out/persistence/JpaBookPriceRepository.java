package uk.co.redsoft.sandbox.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaBookPriceRepository extends JpaRepository<BookPriceEntity, BookPriceId> {

    List<BookPriceEntity> findByIdIsbn(String isbn);
}
