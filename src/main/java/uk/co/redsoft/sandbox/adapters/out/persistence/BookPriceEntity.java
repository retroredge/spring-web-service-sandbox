package uk.co.redsoft.sandbox.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "book_prices")
public class BookPriceEntity {

    @EmbeddedId
    private BookPriceId id;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    protected BookPriceEntity() {}

    public BookPriceEntity(String isbn, String countryCode, BigDecimal price) {
        this.id = new BookPriceId(isbn, countryCode);
        this.price = price;
    }
}
