INSERT INTO roles (name)
VALUES
    ('ROLE_USER'),
    ('ROLE_ORGANIZER'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, email, password, status)
VALUES (
           'admin',
           'admin@eventhub.com',
           '$2a$10$WTqKA3EoSMRrWMa/ZZzZN.adNtD5b8fadbJYCpR3TgCMh.sqhCXoi',
           'ACTIVE'
       )
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT users.id, roles.id
FROM users
         JOIN roles ON roles.name = 'ROLE_ADMIN'
WHERE users.email = 'admin@eventhub.com'
ON CONFLICT DO NOTHING;

INSERT INTO event_categories (name)
VALUES
    ('IT'),
    ('Music'),
    ('Sport'),
    ('Education'),
    ('Business'),
    ('Art')
ON CONFLICT (name) DO NOTHING;

INSERT INTO locations (country, city, address, place_name)
VALUES
    ('Belarus', 'Minsk', 'Independence Ave 10', 'Conference Hall'),
    ('Belarus', 'Minsk', 'Pobediteley Ave 20', 'Event Space'),
    ('Belarus', 'Grodno', 'Sovetskaya Street 5', 'City Cultural Center')
ON CONFLICT DO NOTHING;