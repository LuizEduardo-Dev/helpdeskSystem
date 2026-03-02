CREATE TABLE ticket_comments (
    id          BIGSERIAL PRIMARY KEY,
    ticket_id   BIGINT NOT NULL REFERENCES tickets(id),
    user_id     BIGINT NOT NULL REFERENCES users(id),
    content     TEXT NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);