package uk.co.redsoft.sandbox.adapters.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBookRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String author,
        @NotBlank @Size(max = 20) @Pattern(regexp = "^\\d{3}-\\d{10}$", message = "must be a valid ISBN-13 (e.g. 978-0132350884)") String isbn,
        @NotBlank @Size(max = 100) String genre) {
}
