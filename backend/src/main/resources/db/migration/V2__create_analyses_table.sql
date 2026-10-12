CREATE TABLE analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    headline VARCHAR(500),
    text LONGTEXT NOT NULL,
    source_url VARCHAR(1000),
    label VARCHAR(50) NOT NULL,
    score DOUBLE NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analyses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_analyses_user_created ON analyses (user_id, created_at DESC);
