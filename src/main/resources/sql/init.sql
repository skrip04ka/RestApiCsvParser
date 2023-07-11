drop table if exists meta_inf cascade;

drop table if exists measurements;

CREATE TABLE meta_inf
(
    meta_id    serial PRIMARY KEY,
    name       varchar(100),
    n          int,
    freq       double precision,
    digital    int,
    analog     int,
    time_start varchar(100),
    time_end   varchar(100),
    type       varchar(100)

);

CREATE TABLE measurements
(
    meas_id   serial PRIMARY KEY,
    meta_id   int,
    meas_name varchar(100),
    values    double precision[],
    FOREIGN KEY (meta_id) REFERENCES meta_inf (meta_id)
);