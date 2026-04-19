CREATE TABLE users (
    id   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    age  INTEGER      NOT NULL,
    role VARCHAR(50)  NOT NULL
);

CREATE INDEX idx_users_name ON users (name);
CREATE INDEX idx_users_role ON users (role);
