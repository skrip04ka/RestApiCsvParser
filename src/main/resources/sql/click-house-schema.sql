create database measurement;

create table if not exists measurement.test
(
    timestamp INTEGER,
    IA        DOUBLE,
    IB        DOUBLE,
    IC        DOUBLE
)
    engine = AggregatingMergeTree
        order by (timestamp);

create table if not exists measurement.meas
(
    id        INTEGER,
    name      String,
    tableName String
)
    engine = AggregatingMergeTree
        order by (id);