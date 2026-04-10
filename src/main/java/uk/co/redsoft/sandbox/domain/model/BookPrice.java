package uk.co.redsoft.sandbox.domain.model;

import java.math.BigDecimal;

public record BookPrice(String isbn, String countryCode, BigDecimal price) {}
