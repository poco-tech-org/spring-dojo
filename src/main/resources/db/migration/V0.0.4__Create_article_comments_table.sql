CREATE TABLE article_comments
(
    id BIGINT  PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    body       TEXT   NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(article_id) REFERENCES articles(id)
)