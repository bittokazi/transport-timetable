CREATE TABLE IF NOT EXISTS ${flyway:defaultSchema}.monthly_count (
    id SERIAL NOT NULL,
    user_id int8 NOT NULL,
    year_month varchar(255) NOT NULL,
    request_count int8 NOT NULL
);
