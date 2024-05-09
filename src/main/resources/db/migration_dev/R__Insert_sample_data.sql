DELETE FROM articles;

ALTER TABLE articles AUTO_INCREMENT = 1;

INSERT INTO articles (title, body)
VALUES ('タイトルです1', '1本文です。')
     , ('タイトルです2', '2本文です。')
     , ('タイトルです3', '3本文です。')
;

DELETE FROM users;

INSERT INTO users (username, password, enabled)
VALUES ('user1', 'password', true)
     , ('user2', 'password', true)
     , ('user3', 'password', true)
;
