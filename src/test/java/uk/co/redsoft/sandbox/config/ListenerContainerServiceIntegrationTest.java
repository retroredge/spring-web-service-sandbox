package uk.co.redsoft.sandbox.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.redsoft.sandbox.AbstractWireMockContainersIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ListenerContainerServiceIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    @Autowired
    private ListenerContainerService listenerContainerService;

    @Autowired
    private RabbitListenerEndpointRegistry registry;

    @Test
    void containerIsRunning_atStartup_whenEnabledTrue() {
        var container = registry.getListenerContainer(ListenerContainerService.LISTENER_ID);

        assertThat(container).isNotNull();
        assertThat(container.isRunning()).isTrue();
        assertThat(listenerContainerService.isEnabled()).isTrue();
    }

    @Test
    void disable_stopsContainerInRegistry() {
        listenerContainerService.disable();

        try {
            var container = registry.getListenerContainer(ListenerContainerService.LISTENER_ID);
            assertThat(container.isRunning()).isFalse();
            assertThat(listenerContainerService.isEnabled()).isFalse();
        } finally {
            listenerContainerService.enable();
        }
    }

    @Test
    void enable_afterDisable_restartsContainerInRegistry() {
        listenerContainerService.disable();
        listenerContainerService.enable();

        var container = registry.getListenerContainer(ListenerContainerService.LISTENER_ID);
        assertThat(container.isRunning()).isTrue();
        assertThat(listenerContainerService.isEnabled()).isTrue();
    }
}
