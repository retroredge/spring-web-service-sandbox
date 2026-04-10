package uk.co.redsoft.sandbox.domain.ports.in;

import uk.co.redsoft.sandbox.domain.model.BookDetail;

import java.util.Optional;

public interface BookDetailUseCase {

    Optional<BookDetail> getBookDetail(Long id);
}
