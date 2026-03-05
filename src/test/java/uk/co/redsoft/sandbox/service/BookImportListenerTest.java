package uk.co.redsoft.sandbox.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.repository.BookEntity;
import uk.co.redsoft.sandbox.repository.BookRepository;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookImportListenerTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookImportListener bookImportListener;

    @Test
    void onMessageSavesEntity() {
        var entity = new BookEntity("Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering");

        bookImportListener.onMessage(entity);

        verify(bookRepository).save(entity);
    }
}
