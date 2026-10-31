CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.feedback (
    id SERIAL NOT NULL,
    user_id int8 NOT NULL,
    content varchar(1000) NOT NULL,
    status varchar(255) NOT NULL DEFAULT 'CREATED',
    created_at timestamp,
    updated_at timestamp,
    CONSTRAINT feedback_pkey PRIMARY KEY (id)
);

