DELETE FROM articles;

ALTER TABLE articles AUTO_INCREMENT = 1;

INSERT INTO articles (title, body)
VALUES ('タイトルです1', '1本文です。')
     , ('タイトルです2', '2本文です。')
     , ('タイトルです3', '3本文です。')
;

DELETE FROM users;

-- password is "password" for all users
INSERT INTO users (username, password, enabled)
VALUES ('user1', '$2a$10$B0Ri58yKTh9c1V2D04HR.eYoVtFc13Sycx3hX8LyD4yC0NkT84Vk.', true)
     , ('user2', '$2a$10$w15os/pzx8xfrFkf3P2sbOxGVJ7VyugLsG4EZHE5rUx1Lc7zAfGee', true)
     , ('user3', '$2a$10$Peia4zwXTGmTVPl.frob/ObMRHLZ0RlrbnHhw4tJRh1zwWXX3jOaC', true)
;
