CREATE TABLE password_reset_token (
  id SERIAL NOT NULL,
  user_id int8 NOT NULL,
  token varchar(255) NOT NULL,
  expire_date timestamp,
  PRIMARY KEY (id)
);
