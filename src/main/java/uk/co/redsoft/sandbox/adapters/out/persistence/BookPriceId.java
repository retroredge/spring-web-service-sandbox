package uk.co.redsoft.sandbox.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record BookPriceId(
        String isbn,
        @Column(name = "country_code") String countryCode
) implements Serializable {}
