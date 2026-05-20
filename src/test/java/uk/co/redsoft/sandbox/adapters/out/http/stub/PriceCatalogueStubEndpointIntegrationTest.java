package uk.co.redsoft.sandbox.adapters.out.http.stub;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.redsoft.sandbox.AbstractWireMockContainersIntegrationTest;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PriceCatalogueStubEndpointIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ToggleableClientHttpRequestFactory requestFactory;

    @AfterEach
    void resetStub() {
        requestFactory.setStubEnabled(false);
    }

    @Test
    void getState_returnsStubDisabledByDefault() throws Exception {
        mockMvc.perform(get("/actuator/price-catalogue-stub")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stubEnabled", is(false)));
    }

    @Test
    void postStubEnabled_true_enablesStub() throws Exception {
        mockMvc.perform(post("/actuator/price-catalogue-stub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stubEnabled\": true}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/actuator/price-catalogue-stub")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stubEnabled", is(true)));
    }

    @Test
    void postStubEnabled_false_disablesStub() throws Exception {
        requestFactory.setStubEnabled(true);

        mockMvc.perform(post("/actuator/price-catalogue-stub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stubEnabled\": false}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/actuator/price-catalogue-stub")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stubEnabled", is(false)));
    }
}
