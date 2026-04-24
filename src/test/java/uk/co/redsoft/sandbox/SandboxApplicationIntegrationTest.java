package uk.co.redsoft.sandbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.redsoft.sandbox.adapters.out.persistence.JpaBookPriceRepository;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class SandboxApplicationIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    @Autowired
    private BookUseCase bookUseCase;

    @Autowired
    private JpaBookPriceRepository jpaBookPriceRepository;

    @Test
    void createBookTriggersAsyncPricingAndStoresPrices() {
        bookUseCase.create(new CreateBookCommand(
                "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var prices = jpaBookPriceRepository.findByIsbn("978-0132350884");
            assertThat(prices).hasSize(5);
        });
    }
}
