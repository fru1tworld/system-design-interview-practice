USE fru1t_gsd08;

CREATE TABLE IF NOT EXISTS shorten_urls (
    id BIGINT PRIMARY KEY,
    shorten_url VARCHAR(255) NOT NULL UNIQUE,
    source_url VARCHAR(2083) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shorten_url ON shorten_urls(shorten_url);
CREATE INDEX idx_shorten_url ON shorten_urls(source_url);
