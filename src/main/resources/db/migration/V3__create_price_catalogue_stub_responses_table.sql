CREATE TABLE price_catalogue_stub_responses (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    isbn          VARCHAR(20)  NOT NULL,
    http_status   INT          NOT NULL,
    response_body TEXT         NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_stub_responses_isbn (isbn)
);
