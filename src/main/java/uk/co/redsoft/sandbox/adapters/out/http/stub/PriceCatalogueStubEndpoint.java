package uk.co.redsoft.sandbox.adapters.out.http.stub;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "price-catalogue-stub")
@RequiredArgsConstructor
public class PriceCatalogueStubEndpoint {

    private final ToggleableClientHttpRequestFactory requestFactory;

    @ReadOperation
    public Map<String, Boolean> getState() {
        return Map.of("stubEnabled", requestFactory.isStubEnabled());
    }

    @WriteOperation
    public void setStubEnabled(boolean stubEnabled) {
        requestFactory.setStubEnabled(stubEnabled);
    }
}
