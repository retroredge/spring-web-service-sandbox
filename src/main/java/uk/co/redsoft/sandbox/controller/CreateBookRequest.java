package uk.co.redsoft.sandbox.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateBookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank @Pattern(regexp = "^\\d{3}-\\d{10}$", message = "must be a valid ISBN-13 (e.g. 978-0132350884)") String isbn,
        @NotBlank String genre) {
}
