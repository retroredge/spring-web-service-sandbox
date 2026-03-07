package uk.co.redsoft.sandbox.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBookRepository extends JpaRepository<BookEntity, Long> {
}
