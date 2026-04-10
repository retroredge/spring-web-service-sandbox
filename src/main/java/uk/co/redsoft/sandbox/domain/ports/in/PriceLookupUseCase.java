package uk.co.redsoft.sandbox.domain.ports.in;

public interface PriceLookupUseCase {

    void lookupAndStorePrices(String isbn);
}
