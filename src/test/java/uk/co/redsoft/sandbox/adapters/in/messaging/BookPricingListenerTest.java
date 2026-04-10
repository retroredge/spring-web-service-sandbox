package uk.co.redsoft.sandbox.adapters.in.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.BookPricingMessage;
import uk.co.redsoft.sandbox.domain.ports.in.PriceLookupUseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookPricingListenerTest {

    @Mock
    private PriceLookupUseCase priceLookupUseCase;

    @InjectMocks
    private BookPricingListener bookPricingListener;

    @Test
    void onMessageDelegatesToPriceLookupUseCase() {
        var message = new BookPricingMessage("978-0132350884");

        bookPricingListener.onMessage(message);

        var captor = ArgumentCaptor.forClass(String.class);
        verify(priceLookupUseCase).lookupAndStorePrices(captor.capture());
        assertThat(captor.getValue()).isEqualTo("978-0132350884");
    }
}
