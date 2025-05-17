CREATE SEQUENCE IF NOT EXISTS notification_seq
START WITH 101
INCREMENT BY 1
MINVALUE 101
MAXVALUE 10000000000
CYCLE;

CREATE TABLE IF NOT EXISTS notification(
  id BIGINT PRIMARY KEY NOT NULL DEFAULT NEXTVAL('notification_seq'),
  message VARCHAR(255) NOT NULL,
  timestamp TIMESTAMP,
  user_id BIGINT REFERENCES _user(id) NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS client_seq
START WITH 101
INCREMENT BY 1
MINVALUE 101
MAXVALUE 10000000000
NO CYCLE;

CREATE TABLE IF NOT EXISTS client(
  id BIGINT PRIMARY KEY NOT NULL DEFAULT NEXTVAL('client_seq'),
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  phone VARCHAR(255),
  created_at TIMESTAMP,
  last_modified_at TIMESTAMP
);

ALTER TABLE ticket ADD COLUMN client_id BIGINT REFERENCES client(id);

INSERT INTO authority (authority) VALUES ('client::create');
INSERT INTO authority (authority) VALUES ('client::edit');
INSERT INTO authority (authority) VALUES ('client::delete');

INSERT INTO role_authority (role_id, authority_id) VALUES (101, (SELECT id FROM authority WHERE authority = 'client::create'));
INSERT INTO role_authority (role_id, authority_id) VALUES (101, (SELECT id FROM authority WHERE authority = 'client::edit'));
INSERT INTO role_authority (role_id, authority_id) VALUES (101, (SELECT id FROM authority WHERE authority = 'client::delete'));

