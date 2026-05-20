package uk.co.redsoft.sandbox.adapters.in.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PrimeStubResponseRequest(
        @NotBlank
        @Pattern(regexp = "^\\d{3}-\\d{10}$", message = "must match ISBN-13 format (e.g. 978-0132350884)")
        String isbn,

        @NotNull
        @Min(100)
        @Max(599)
        Integer httpStatus,

        @NotNull
        String responseBody
) {
}
