-- Employee table
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
);

-- Visitor table
CREATE TABLE visitor (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address VARCHAR(255),
    picture_path VARCHAR(255),
    host_id BIGINT REFERENCES employee(id),
    created_at TIMESTAMP NOT NULL
);

-- Visit table
CREATE TABLE visit (
    id BIGSERIAL PRIMARY KEY,
    visitor_id BIGINT NOT NULL REFERENCES visitor(id),
    host VARCHAR(255),
    otp VARCHAR(20),
    is_approved BOOLEAN DEFAULT FALSE,
    visit_date TIMESTAMP NOT NULL
);

-- Otp table
CREATE TABLE otp (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    visit_id BIGINT REFERENCES visit(id),
    resend_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

-- Attachment table
CREATE TABLE attachment (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    data BYTEA,
    content_type VARCHAR(255)
);

-- Email table
CREATE TABLE email (
    id BIGSERIAL PRIMARY KEY,
    sender VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL
);