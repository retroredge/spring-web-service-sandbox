package uk.co.redsoft.sandbox.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "book_prices")
public class BookPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isbn;

    @Column(name = "country_code")
    private String countryCode;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    protected BookPriceEntity() {}

    public BookPriceEntity(String isbn, String countryCode, BigDecimal price) {
        this.isbn = isbn;
        this.countryCode = countryCode;
        this.price = price;
    }
}
