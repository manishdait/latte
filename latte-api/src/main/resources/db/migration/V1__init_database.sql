CREATE SEQUENCE IF NOT EXISTS authority_seq
START WITH 101
INCREMENT BY 1
MINVALUE 101
MAXVALUE 10000
NO CYCLE;

CREATE TABLE IF NOT EXISTS authority (
  id BIGINT PRIMARY KEY NOT NULL DEFAULT NEXTVAL('authority_seq'),
  authority VARCHAR(255) UNIQUE NOT NULL
);

INSERT INTO authority (authority) VALUES ('user::create');
INSERT INTO authority (authority) VALUES ('user::edit');
INSERT INTO authority (authority) VALUES ('user::delete');
INSERT INTO authority (authority) VALUES ('user::reset-password');
INSERT INTO authority (authority) VALUES ('ticket::create');
INSERT INTO authority (authority) VALUES ('ticket::edit');
INSERT INTO authority (authority) VALUES ('ticket::delete');
INSERT INTO authority (authority) VALUES ('ticket::lock-unlock');
INSERT INTO authority (authority) VALUES ('ticket::assign');
INSERT INTO authority (authority) VALUES ('role::create');
INSERT INTO authority (authority) VALUES ('role::edit');
INSERT INTO authority (authority) VALUES ('role::delete');

CREATE SEQUENCE IF NOT EXISTS role_seq
START WITH 101
INCREMENT BY 1
MINVALUE 101
MAXVALUE 10000
NO CYCLE;

CREATE TABLE IF NOT EXISTS role (
  id BIGINT PRIMARY KEY NOT NULL DEFAULT NEXTVAL('role_seq'),
  role VARCHAR(255) UNIQUE NOT NULL
);

INSERT INTO role (role) VALUES ('Admin');
INSERT INTO role (role) VALUES ('User');

CREATE TABLE IF NOT EXISTS role_authority (
  role_id BIGINT REFERENCES role(id),
  authority_id BIGINT REFERENCES authority(id)
);

INSERT INTO role_authority (role_id, authority_id) VALUES (101, 101);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 102);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 103);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 104);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 105);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 106);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 107);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 108);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 109);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 110);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 111);
INSERT INTO role_authority (role_id, authority_id) VALUES (101, 112);

INSERT INTO role_authority (role_id, authority_id) VALUES (102, 105);
INSERT INTO role_authority (role_id, authority_id) VALUES (102, 106);

CREATE SEQUENCE IF NOT EXISTS user_seq
START WITH 101
INCREMENT BY 1
MINVALUE 101
MAXVALUE 10000000000
NO CYCLE;

CREATE TABLE IF NOT EXISTS _user (
  id BIGINT PRIMARY KEY NOT NULL DEFAULT NEXTVAL('user_seq'),
  first_name VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role_id BIGINT REFERENCES role(id) NOT NULL,
  deletable BOOLEAN NOT NULL DEFAULT true,
  editable BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP,
  last_modified_at TIMESTAMP
);

CREATE SEQUENCE IF NOT EXISTS ticket_seq
START WITH 101
INCREMENT BY 1
MINVALUE 101
MAXVALUE 100000000000
NO CYCLE;

CREATE TABLE IF NOT EXISTS ticket (
  id BIGINT PRIMARY KEY NOT NULL DEFAULT NEXTVAL('ticket_seq'),
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  priority VARCHAR(255) CHECK(priority = 'LOW' OR priority = 'MEDIUM' OR priority = 'HIGH') NOT NULL,
  status VARCHAR(255) CHECK(status = 'OPEN' OR status = 'CLOSE' OR status = 'IN_PROGRESS') NOT NULL,
  lock Boolean NOT NULL DEFAULT FALSE,
  created_by BIGINT REFERENCES _user(id) NOT NULL,
  assigned_to BIGINT REFERENCES _user(id),
  created_at TIMESTAMP,
  last_modified_at TIMESTAMP
);

CREATE SEQUENCE IF NOT EXISTS activity_seq
START WITH 101
INCREMENT BY 1
MINVALUE 101
MAXVALUE 100000000000
NO CYCLE;

CREATE TABLE IF NOT EXISTS activity (
  id BIGINT PRIMARY KEY NOT NULL DEFAULT NEXTVAL('activity_seq'),
  type VARCHAR(255) CHECK(type = 'EDIT' OR type = 'COMMENT') NOT NULL,
  message VARCHAR(255) NOT NULL,
  author_id BIGINT REFERENCES _user(id) NOT NULL,
  ticket_id BIGINT REFERENCES ticket(id) NOT NULL,
  created_at TIMESTAMP,
  last_modified_at TIMESTAMP
);
