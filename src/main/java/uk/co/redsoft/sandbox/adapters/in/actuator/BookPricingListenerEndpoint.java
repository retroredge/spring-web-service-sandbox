package uk.co.redsoft.sandbox.adapters.in.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.config.ListenerContainerService;

import java.util.Map;

@Component
@Endpoint(id = "book-pricing-listener")
public class BookPricingListenerEndpoint {

    private final ListenerContainerService listenerContainerService;

    public BookPricingListenerEndpoint(ListenerContainerService listenerContainerService) {
        this.listenerContainerService = listenerContainerService;
    }

    @ReadOperation
    public Map<String, Object> getState() {
        return Map.of("enabled", listenerContainerService.isEnabled());
    }

    @WriteOperation
    public Map<String, Object> setState(@Selector String action) {
        return switch (action) {
            case "enable" -> {
                listenerContainerService.enable();
                yield Map.of("enabled", true);
            }
            case "disable" -> {
                listenerContainerService.disable();
                yield Map.of("enabled", false);
            }
            default -> throw new IllegalArgumentException("Unknown action: " + action + ". Use 'enable' or 'disable'.");
        };
    }
}
