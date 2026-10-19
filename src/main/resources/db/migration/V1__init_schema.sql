CREATE TABLE app_user (
    id            UUID PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE pokemon (
    id           INTEGER PRIMARY KEY,
    name         VARCHAR(100)     NOT NULL UNIQUE,
    image_url    TEXT,
    category     VARCHAR(150),
    description  TEXT,
    weight_kg    DOUBLE PRECISION NOT NULL,
    abilities    TEXT[]           NOT NULL DEFAULT '{}',
    stats        JSONB            NOT NULL DEFAULT '[]',
    local_name   VARCHAR(150),
    region       VARCHAR(150),
    tags         TEXT[]           NOT NULL DEFAULT '{}',
    synced_at    TIMESTAMPTZ      NOT NULL
);

CREATE INDEX idx_pokemon_name ON pokemon (name);
