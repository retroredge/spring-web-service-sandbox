package uk.co.redsoft.sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.redsoft.sandbox.repository.BookEntity;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
}
