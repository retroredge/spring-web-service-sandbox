package uk.co.redsoft.sandbox.domain.model;

import java.math.BigDecimal;

public record BookDetail(Long id, String title, String author, String isbn, String genre, BigDecimal gbrPrice) {}
