package uk.co.redsoft.sandbox.adapters.in.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.redsoft.sandbox.AbstractWireMockContainersIntegrationTest;
import uk.co.redsoft.sandbox.adapters.out.persistence.JpaBookPriceRepository;
import uk.co.redsoft.sandbox.config.ListenerContainerService;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.domain.model.BookPricingMessage;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookPricingListenerEndpointIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListenerContainerService listenerContainerService;

    @Autowired
    private RabbitListenerEndpointRegistry registry;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private JpaBookPriceRepository jpaBookPriceRepository;

    @BeforeEach
    void cleanPrices() {
        jpaBookPriceRepository.deleteAll();
    }

    @Test
    void getState_returnsEnabledTrue_byDefault() throws Exception {
        mockMvc.perform(get("/actuator/book-pricing-listener")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(true)));
    }

    @Test
    void disable_stopsListenerContainer() throws Exception {
        mockMvc.perform(post("/actuator/book-pricing-listener/disable")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)));

        try {
            var container = registry.getListenerContainer(ListenerContainerService.LISTENER_ID);
            assertThat(container.isRunning()).isFalse();
        } finally {
            listenerContainerService.enable();
        }
    }

    @Test
    void enable_afterDisable_restartsListenerContainer() throws Exception {
        listenerContainerService.disable();

        mockMvc.perform(post("/actuator/book-pricing-listener/enable")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(true)));

        var container = registry.getListenerContainer(ListenerContainerService.LISTENER_ID);
        assertThat(container.isRunning()).isTrue();
    }

    @Test
    void messagesAccumulate_whenListenerDisabled_thenConsumed_whenReEnabled() {
        var isbn = "978-0321125217";

        listenerContainerService.disable();
        try {
            amqpTemplate.convertAndSend(RabbitConfig.PRICING_QUEUE, new BookPricingMessage(isbn));

            // Verify message is NOT consumed while disabled
            await().during(2, TimeUnit.SECONDS)
                    .atMost(3, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            assertThat(jpaBookPriceRepository.findByIsbn(isbn)).isEmpty()
                    );
        } finally {
            listenerContainerService.enable();
        }

        // Verify message IS consumed after re-enabling
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(jpaBookPriceRepository.findByIsbn(isbn)).isNotEmpty()
                );
    }
}
