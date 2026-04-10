package uk.co.redsoft.sandbox.adapters.out.http;

import java.util.List;

public record PriceCatalogueResponse(String status, String isbn, List<BookPriceResponse> prices) {}
