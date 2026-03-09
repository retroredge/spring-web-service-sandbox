package uk.co.redsoft.sandbox.domain.model;

public record CreateBookCommand(String title, String author, String isbn, String genre) {

    public CreateBookCommand {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be blank");
        if (author == null || author.isBlank()) throw new IllegalArgumentException("author must not be blank");
        if (isbn == null || isbn.isBlank()) throw new IllegalArgumentException("isbn must not be blank");
        if (genre == null || genre.isBlank()) throw new IllegalArgumentException("genre must not be blank");
    }
}
