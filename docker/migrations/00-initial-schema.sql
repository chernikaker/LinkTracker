CREATE TABLE user(
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL
)

CREATE TYPE link_type AS ENUM ('GITHUB', 'STACKOVERFLOW');

CREATE TABLE link(
    id BIGSERIAL PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    type link_type NOT NULL,
    last_validation TIMESTAMP
)

CREATE TABLE subscription(
   id BIGSERIAL PRIMARY KEY,
   user_id BIGINT NOT NULL,
   link_id BIGINT NOT NULL,
   FOREIGN KEY (user_id) REFERENCES User(id),
   FOREIGN KEY (link_id) REFERENCES Link(id)
)

CREATE TABLE tag(
    id BIGSERIAL PRIMARY KEY,
    tag_text VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(id)
)

CREATE TABLE filter(
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(50) NOT NULL,
    value VARCHAR(50) NOT NULL
)

CREATE TABLE subscription_tag(
    subscription_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (subscription_id, tag_id),
    FOREIGN KEY (subscription_id) REFERENCES subscription(id),
    FOREIGN KEY (tag_id) REFERENCES tag(id)
)

CREATE TABLE subscription_tag(
    subscription_id BIGINT NOT NULL,
    filter_id BIGINT NOT NULL
    PRIMARY KEY (subscription_id, filter_id)
    FOREIGN KEY (subscription_id) REFERENCES subscription(id),
    FOREIGN KEY (filter_id) REFERENCES filter(id)
)
