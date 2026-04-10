package uk.co.redsoft.sandbox.adapters.out.http;

public class PriceCatalogueException extends RuntimeException {

    PriceCatalogueException(int statusCode) {
        super("Price catalogue error: HTTP " + statusCode);
    }
}
