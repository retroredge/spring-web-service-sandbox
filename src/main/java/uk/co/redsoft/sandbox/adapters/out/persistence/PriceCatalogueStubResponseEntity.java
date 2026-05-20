package uk.co.redsoft.sandbox.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.time.Instant;

@Getter
@Entity
@Table(name = "price_catalogue_stub_responses")
public class PriceCatalogueStubResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isbn;

    @Column(name = "http_status")
    private int httpStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Generated(GenerationTime.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected PriceCatalogueStubResponseEntity() {}

    public PriceCatalogueStubResponseEntity(String isbn, int httpStatus, String responseBody) {
        this.isbn = isbn;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    public PriceCatalogueStubResponseEntity(Long id, String isbn, int httpStatus, String responseBody) {
        this.id = id;
        this.isbn = isbn;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }
}
