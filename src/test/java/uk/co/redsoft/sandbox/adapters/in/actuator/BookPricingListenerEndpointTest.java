package uk.co.redsoft.sandbox.adapters.in.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.config.ListenerContainerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookPricingListenerEndpointTest {

    @Mock
    private ListenerContainerService listenerContainerService;

    @InjectMocks
    private BookPricingListenerEndpoint endpoint;

    @Test
    void getState_returnsEnabledTrue_whenServiceReportsEnabled() {
        when(listenerContainerService.isEnabled()).thenReturn(true);

        var result = endpoint.getState();

        assertThat(result).containsEntry("enabled", true);
    }

    @Test
    void getState_returnsEnabledFalse_whenServiceReportsDisabled() {
        when(listenerContainerService.isEnabled()).thenReturn(false);

        var result = endpoint.getState();

        assertThat(result).containsEntry("enabled", false);
    }

    @Test
    void setState_enable_callsServiceEnableAndReturnsEnabledTrue() {
        var result = endpoint.setState("enable");

        verify(listenerContainerService).enable();
        assertThat(result).containsEntry("enabled", true);
    }

    @Test
    void setState_disable_callsServiceDisableAndReturnsEnabledFalse() {
        var result = endpoint.setState("disable");

        verify(listenerContainerService).disable();
        assertThat(result).containsEntry("enabled", false);
    }

    @Test
    void setState_unknownAction_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> endpoint.setState("pause"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pause");
    }
}
