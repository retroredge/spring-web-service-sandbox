CREATE TABLE book_prices (
    isbn         VARCHAR(20)    NOT NULL,
    country_code CHAR(3)        NOT NULL,
    price        DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (isbn, country_code)
);
