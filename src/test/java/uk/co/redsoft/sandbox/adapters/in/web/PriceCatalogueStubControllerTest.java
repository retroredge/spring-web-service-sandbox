package uk.co.redsoft.sandbox.adapters.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.redsoft.sandbox.domain.ports.in.PriceCatalogueStubUseCase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceCatalogueStubController.class)
class PriceCatalogueStubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceCatalogueStubUseCase stubUseCase;

    @Test
    void primeReturns204AndDelegatestoUseCase() throws Exception {
        mockMvc.perform(post("/price-catalogue-stub/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0132350884","httpStatus":200,"responseBody":"{\\"status\\":\\"ok\\"}"}
                                """))
                .andExpect(status().isNoContent());

        verify(stubUseCase).prime(any());
    }

    @Test
    void primeReturns400WhenIsbnIsBlank() throws Exception {
        mockMvc.perform(post("/price-catalogue-stub/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"","httpStatus":200,"responseBody":"{}"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void primeReturns400WhenIsbnFormatIsInvalid() throws Exception {
        mockMvc.perform(post("/price-catalogue-stub/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"not-an-isbn","httpStatus":200,"responseBody":"{}"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void primeReturns400WhenHttpStatusIsBelowRange() throws Exception {
        mockMvc.perform(post("/price-catalogue-stub/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0132350884","httpStatus":99,"responseBody":"{}"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void primeReturns400WhenHttpStatusIsAboveRange() throws Exception {
        mockMvc.perform(post("/price-catalogue-stub/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0132350884","httpStatus":600,"responseBody":"{}"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void primeReturns400WhenResponseBodyIsNull() throws Exception {
        mockMvc.perform(post("/price-catalogue-stub/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0132350884","httpStatus":200}
                                """))
                .andExpect(status().isBadRequest());
    }
}
