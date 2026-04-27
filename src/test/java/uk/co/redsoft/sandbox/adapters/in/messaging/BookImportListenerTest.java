package uk.co.redsoft.sandbox.adapters.in.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookWriteUseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookImportListenerTest {

    @Mock
    private BookWriteUseCase bookWriteUseCase;

    @InjectMocks
    private BookImportListener bookImportListener;

    @Test
    void onMessageCreatesBook() {
        var command = new CreateBookCommand("Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering");

        bookImportListener.onMessage(command);

        var captor = ArgumentCaptor.forClass(CreateBookCommand.class);
        verify(bookWriteUseCase).create(captor.capture());
        assertThat(captor.getValue().title()).isEqualTo("Clean Code");
        assertThat(captor.getValue().isbn()).isEqualTo("978-0132350884");
    }
}
