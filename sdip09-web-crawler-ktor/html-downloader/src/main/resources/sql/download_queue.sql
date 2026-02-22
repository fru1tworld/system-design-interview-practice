CREATE TABLE download_queue (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    original_url VARCHAR(65535),
    root_domain VARCHAR(255),
    depth INT,
    bfs_path VARCHAR(65535),
    status VARCHAR(20),
    score BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);