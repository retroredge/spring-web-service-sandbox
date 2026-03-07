package uk.co.redsoft.sandbox.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.model.BookImportMessage;
import uk.co.redsoft.sandbox.repository.BookEntity;
import uk.co.redsoft.sandbox.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookImportListenerTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookImportListener bookImportListener;

    @Test
    void onMessageSavesEntity() {
        var message = new BookImportMessage("Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering");

        bookImportListener.onMessage(message);

        var captor = ArgumentCaptor.forClass(BookEntity.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Clean Code");
        assertThat(captor.getValue().getIsbn()).isEqualTo("978-0132350884");
    }
}
