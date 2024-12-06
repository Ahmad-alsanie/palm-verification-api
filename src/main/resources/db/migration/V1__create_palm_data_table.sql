CREATE TABLE IF NOT EXISTS palm_data (
    palm_id UUID PRIMARY KEY,
    school_id VARCHAR(255) NOT NULL,
    palm_binary BYTEA NOT NULL
);