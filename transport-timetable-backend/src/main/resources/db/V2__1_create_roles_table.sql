CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.role (
  id SERIAL NOT NULL,
  name varchar(255) NOT NULL,
  role_key varchar(255) NOT NULL,
  CONSTRAINT role_role_key UNIQUE (role_key)
);

INSERT INTO ${flyway:defaultSchema}.role (name, role_key)
VALUES('Super Admin', 'ROLE_SUPER_ADMIN');

INSERT INTO ${flyway:defaultSchema}.role (name, role_key)
VALUES('User', 'ROLE_USER');
