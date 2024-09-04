DELETE FROM users;

ALTER TABLE users AUTO_INCREMENT = 1;

-- password is "password00" for all users
INSERT INTO users (id, username, password, enabled)
VALUES (1, 'user1', '$2a$10$GOMqj9gVaqQv8Bj1pgxCbeX3gH4cn.R6EKE8rJ7pdAoOEv1bU28BO', true)
     , (2, 'user2', '$2a$10$Q0dkYjfDmw9Sl7hpNZSJr.kDtnAzfl8LswqS25ql.ZwVf0GLFgYZ.', true)
     , (3, 'user3', '$2a$10$1BH.E52KOmoV4ADr5/AZreMrvIjMPkKy3hMJYZTRhsLEbITHiuUd2', true)
;

DELETE FROM articles;

ALTER TABLE articles AUTO_INCREMENT = 1;

INSERT INTO articles (title, body, user_id)
VALUES ('タイトルです1', '1本文です。', 1)
     , ('タイトルです2', '2本文です。', 1)
     , ('タイトルです3', '3本文です。', 2)
;

