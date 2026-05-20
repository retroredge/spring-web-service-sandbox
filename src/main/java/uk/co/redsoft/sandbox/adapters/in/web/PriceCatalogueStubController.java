package uk.co.redsoft.sandbox.adapters.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.redsoft.sandbox.domain.model.PriceCatalogueStubResponse;
import uk.co.redsoft.sandbox.domain.ports.in.PriceCatalogueStubUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/price-catalogue-stub")
@Tag(name = "Price Catalogue Stub")
public class PriceCatalogueStubController {

    private final PriceCatalogueStubUseCase stubUseCase;

    @PostMapping("/responses")
    @Operation(summary = "Prime a stub response for a given ISBN")
    public ResponseEntity<Void> prime(@Valid @RequestBody PrimeStubResponseRequest request) {
        stubUseCase.prime(new PriceCatalogueStubResponse(request.isbn(), request.httpStatus(), request.responseBody()));
        return ResponseEntity.noContent().build();
    }
}
