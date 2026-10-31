CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.user_role (
  user_id int8 NOT NULL,
  role_id int8 NOT NULL,
  CONSTRAINT uq_userid_roleid UNIQUE (user_id, role_id)
);

INSERT INTO ${flyway:defaultSchema}.user_role (user_id, role_id)
VALUES(1, 1);
