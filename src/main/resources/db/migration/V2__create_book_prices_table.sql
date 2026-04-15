CREATE TABLE book_prices (
    id           BIGINT         AUTO_INCREMENT PRIMARY KEY,
    isbn         VARCHAR(20)    NOT NULL,
    country_code CHAR(3)        NOT NULL,
    price        DECIMAL(10, 2) NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_book_prices_isbn_country (isbn, country_code)
);
