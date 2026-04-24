package uk.co.redsoft.sandbox.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListenerContainerServiceTest {

    @Mock
    private RabbitListenerEndpointRegistry registry;

    @Mock
    private MessageListenerContainer container;

    @Test
    void applyInitialState_doesNotStopContainer_whenEnabledIsTrue() {
        var service = new ListenerContainerService(registry, true);
        service.applyInitialState();

        verify(container, never()).stop();
        verifyNoInteractions(registry);
    }

    @Test
    void applyInitialState_stopsContainer_whenEnabledIsFalse() {
        when(registry.getListenerContainer(ListenerContainerService.LISTENER_ID)).thenReturn(container);
        when(container.isRunning()).thenReturn(true);

        var service = new ListenerContainerService(registry, false);
        service.applyInitialState();

        verify(container).stop();
    }

    @Test
    void disable_stopsRunningContainer_andSetsEnabledFalse() {
        when(registry.getListenerContainer(ListenerContainerService.LISTENER_ID)).thenReturn(container);
        when(container.isRunning()).thenReturn(true);
        var service = new ListenerContainerService(registry, true);

        service.disable();

        verify(container).stop();
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    void disable_doesNotCallStop_whenContainerAlreadyStopped() {
        when(registry.getListenerContainer(ListenerContainerService.LISTENER_ID)).thenReturn(container);
        when(container.isRunning()).thenReturn(false);
        var service = new ListenerContainerService(registry, true);

        service.disable();

        verify(container, never()).stop();
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    void enable_startsStoppedContainer_andSetsEnabledTrue() {
        when(registry.getListenerContainer(ListenerContainerService.LISTENER_ID)).thenReturn(container);
        when(container.isRunning()).thenReturn(false);
        var service = new ListenerContainerService(registry, false);

        service.enable();

        verify(container).start();
        assertThat(service.isEnabled()).isTrue();
    }

    @Test
    void enable_doesNotCallStart_whenContainerAlreadyRunning() {
        when(registry.getListenerContainer(ListenerContainerService.LISTENER_ID)).thenReturn(container);
        when(container.isRunning()).thenReturn(true);
        var service = new ListenerContainerService(registry, true);

        service.enable();

        verify(container, never()).start();
        assertThat(service.isEnabled()).isTrue();
    }

    @Test
    void disable_then_enable_cyclesContainerCorrectly() {
        when(registry.getListenerContainer(ListenerContainerService.LISTENER_ID)).thenReturn(container);
        when(container.isRunning()).thenReturn(true, false);
        var service = new ListenerContainerService(registry, true);

        service.disable();
        service.enable();

        verify(container).stop();
        verify(container).start();
        assertThat(service.isEnabled()).isTrue();
    }
}
