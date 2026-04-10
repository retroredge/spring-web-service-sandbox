package uk.co.redsoft.sandbox.adapters.out.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record BookPriceResponse(@JsonProperty("country_code") String countryCode, BigDecimal price) {}
