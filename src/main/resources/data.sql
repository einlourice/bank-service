INSERT INTO users (name, email, password)
VALUES ('User #1', 'user.1@example.com', '$2a$10$7w6MhmOq3rgJ0gsTKzkQe.GlQexQgq1hptE62KQ4L0xoM1H8Lflle');

INSERT INTO users (name, email, password) VALUES
    ('User #2', 'user.2@example.com', '$2a$10$7w6MhmOq3rgJ0gsTKzkQe.GlQexQgq1hptE62KQ4L0xoM1H8Lflle');

INSERT INTO cards (card_type, card_number) VALUES
    ('DEBIT', '4111111111111111'),
    ('CREDIT', '5555555555554444');

INSERT INTO accounts (user_id, card_id, balance)
SELECT u.id, c.id, 1000.00
FROM users u, cards c
WHERE u.email = 'user.1@example.com'
AND c.card_number = '4111111111111111';

INSERT INTO accounts (user_id, card_id, balance)
SELECT u.id, c.id, 2000.00
FROM users u, cards c
WHERE u.email = 'user.1@example.com'
AND c.card_number = '5555555555554444';

INSERT INTO cards (card_type, card_number) VALUES
    ('DEBIT', '4111111111111112'),
    ('CREDIT', '5555555555554445');

INSERT INTO accounts (user_id, card_id, balance)
SELECT u.id, c.id, 1000.00
FROM users u, cards c
WHERE u.email = 'user.2@example.com'
AND c.card_number = '4111111111111112';

INSERT INTO accounts (user_id, card_id, balance)
SELECT u.id, c.id, 2000.00
FROM users u, cards c
WHERE u.email = 'user.2@example.com'
AND c.card_number = '5555555555554445';