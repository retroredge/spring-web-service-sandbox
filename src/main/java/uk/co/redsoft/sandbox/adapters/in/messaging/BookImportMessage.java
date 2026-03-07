package uk.co.redsoft.sandbox.adapters.in.messaging;

public record BookImportMessage(String title, String author, String isbn, String genre) {
}
