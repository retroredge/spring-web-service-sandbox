package uk.co.redsoft.sandbox.domain.model;

public record CreateBookCommand(String title, String author, String isbn, String genre) {}
