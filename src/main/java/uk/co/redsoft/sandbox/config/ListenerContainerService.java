package uk.co.redsoft.sandbox.config;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ListenerContainerService {

    public static final String LISTENER_ID = "bookPricingListener";

    private final RabbitListenerEndpointRegistry registry;
    private volatile boolean enabled;

    public ListenerContainerService(RabbitListenerEndpointRegistry registry,
            @Value("${messaging.pricing-listener.enabled:true}") boolean initialEnabled) {
        this.registry = registry;
        this.enabled = initialEnabled;
    }

    @PostConstruct
    void applyInitialState() {
        if (!enabled) {
            var container = registry.getListenerContainer(LISTENER_ID);
            if (container != null && container.isRunning()) {
                container.stop();
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
        var container = registry.getListenerContainer(LISTENER_ID);
        if (container != null && !container.isRunning()) {
            container.start();
        }
    }

    public void disable() {
        enabled = false;
        var container = registry.getListenerContainer(LISTENER_ID);
        if (container != null && container.isRunning()) {
            container.stop();
        }
    }

}
