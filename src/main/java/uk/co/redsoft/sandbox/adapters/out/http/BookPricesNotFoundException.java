package uk.co.redsoft.sandbox.adapters.out.http;

class BookPricesNotFoundException extends RuntimeException {

    BookPricesNotFoundException(String isbn) {
        super("No prices found for ISBN: " + isbn);
    }
}
