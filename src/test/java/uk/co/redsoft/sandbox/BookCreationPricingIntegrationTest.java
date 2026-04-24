package uk.co.redsoft.sandbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.ports.in.BookDetailUseCase;
import uk.co.redsoft.sandbox.adapters.out.messaging.RabbitBookPricingPublishAdapter;
import uk.co.redsoft.sandbox.domain.ports.out.BookRepositoryPort;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class BookCreationPricingIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    @Autowired
    private BookRepositoryPort bookRepositoryPort;

    @Autowired
    private RabbitBookPricingPublishAdapter bookPricingPublishPort;

    @Autowired
    private BookDetailUseCase bookDetailUseCase;

    @Test
    void creatingBookTriggersAsyncPricingVisibleViaBookDetail() {
        var book = bookRepositoryPort.save(
                new Book(null, "Domain-Driven Design", "Eric Evans", "978-0321125217", "Software Architecture"));
        bookPricingPublishPort.publish(book.isbn());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var detail = bookDetailUseCase.getBookDetail(book.id());
            assertThat(detail).isPresent();
            assertThat(detail.get().gbrPrice()).isEqualByComparingTo(new BigDecimal("44.99"));
        });
    }
}
