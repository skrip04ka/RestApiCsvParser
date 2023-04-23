create database measurement;

create table if not exists measurement.test
(
    timestamp DOUBLE,
    IA      DOUBLE,
    IB      DOUBLE,
    IC      DOUBLE
)

engine = AggregatingMergeTree
order by (timestamp);
