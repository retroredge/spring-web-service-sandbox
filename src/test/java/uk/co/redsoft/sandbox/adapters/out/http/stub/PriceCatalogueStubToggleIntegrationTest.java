package uk.co.redsoft.sandbox.adapters.out.http.stub;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.redsoft.sandbox.AbstractWireMockContainersIntegrationTest;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PriceCatalogueStubToggleIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    private static final String ISBN = "978-0132350884";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PriceCataloguePort priceCataloguePort;

    @Autowired
    private ToggleableClientHttpRequestFactory requestFactory;

    @AfterEach
    void resetStub() {
        requestFactory.setStubEnabled(false);
    }

    @Test
    void stubResponseReturnedWhenEnabled_realResponseReturnedWhenDisabled() throws Exception {
        mockMvc.perform(post("/price-catalogue-stub/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "978-0132350884",
                                  "httpStatus": 200,
                                  "responseBody": "{\\"status\\":\\"ok\\",\\"isbn\\":\\"978-0132350884\\",\\"prices\\":[{\\"country_code\\":\\"GBR\\",\\"price\\":99.99}]}"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/actuator/price-catalogue-stub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stubEnabled\":true}"))
                .andExpect(status().isNoContent());

        var stubPrices = priceCataloguePort.fetchPrices(ISBN);
        var gbrStubPrice = stubPrices.stream().filter(p -> "GBR".equals(p.countryCode())).findFirst();
        assertThat(gbrStubPrice).isPresent();
        assertThat(gbrStubPrice.get().price()).isEqualByComparingTo(new BigDecimal("99.99"));

        mockMvc.perform(post("/actuator/price-catalogue-stub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stubEnabled\":false}"))
                .andExpect(status().isNoContent());

        var realPrices = priceCataloguePort.fetchPrices(ISBN);
        var gbrRealPrice = realPrices.stream().filter(p -> "GBR".equals(p.countryCode())).findFirst();
        assertThat(gbrRealPrice).isPresent();
        assertThat(gbrRealPrice.get().price()).isEqualByComparingTo(new BigDecimal("24.99"));
    }
}
