# Web Crawler Project Guidelines

## Development Environment
- **Java Version**: Java 24 (all modules must use Java 24)
- **Spring Boot Version**: 3.5.5
- **Build Tool**: Gradle

## Primary Key Strategy
- All PKs use UUIDv7 for better performance and time-based ordering

## Database Development Guidelines
- Always refer to SQL files in resources/sql directory when developing
- Never modify existing table structures
- Use the provided DDL as reference for entity development

## Download Queue Table Structure
```sql
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
```