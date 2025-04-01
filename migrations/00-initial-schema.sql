CREATE TABLE tg_user (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE
);

CREATE TABLE link (
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(2048) NOT NULL UNIQUE,
    last_validation TIMESTAMP NOT NULL
);

CREATE TABLE subscription (
   id BIGSERIAL PRIMARY KEY,
   user_id BIGINT NOT NULL,
   link_id BIGINT NOT NULL,
   FOREIGN KEY (user_id) REFERENCES tg_user(id) ON DELETE CASCADE,
   FOREIGN KEY (link_id) REFERENCES link(id) ON DELETE CASCADE
);

CREATE TABLE tag (
    id BIGSERIAL PRIMARY KEY,
    tag_text VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES tg_user(id) ON DELETE CASCADE
);

CREATE TABLE filter (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(50) NOT NULL,
    value VARCHAR(50) NOT NULL,
    subscription_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (subscription_id) REFERENCES subscription(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES tg_user(id) ON DELETE CASCADE
);

CREATE TABLE subscription_tag (
    subscription_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (subscription_id, tag_id),
    FOREIGN KEY (subscription_id) REFERENCES subscription(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);
