drop table if exists measurement.meas;

create table if not exists measurement.meas
(
    id      INTEGER,
    time_id INTEGER,
    time    DOUBLE,
    values Map(String, DOUBLE)
)
    ENGINE = MergeTree order by (id);

drop table if exists measurement.meta_inf;

create table if not exists measurement.meta_inf
(
    id   INTEGER,
    name String,
    meta String,
    names Array(String)
)
    ENGINE = MergeTree order by (id);