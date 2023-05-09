package ru.mpei.parser.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import ru.mpei.parser.model.MeasList;
import ru.mpei.parser.model.Measurements;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.measurement.AnalogMeas;
import ru.mpei.parser.model.measurement.DigitalMeas;
import ru.mpei.parser.model.measurement.ThreeMeasData;
import ru.mpei.parser.util.JsonParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Repository
public class ClickHouseRepository {

    @Value("${parser.digitalSuffix}")
    private String digitalSuffix = "BOOL";
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Value("${clickhouse.table.name}")
    private String table;

    @Autowired
    public ClickHouseRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public void saveMeas(List<Measurements> measurements, MetaInf metaInf) {
        namedParameterJdbcTemplate.update(
                "drop table if exists " + table,
                Map.of());


        namedParameterJdbcTemplate.update("create table if not exists " + table + " (id INTEGER, time DOUBLE ) " +
                        "ENGINE = MergeTree order by (id) COMMENT '" + JsonParser.dataToString(metaInf) + "'",
                Map.of());


        log.info("table {} create with meta inf {}", table, metaInf);

        StringJoiner joinerParam = new StringJoiner(", :", ":", "");
        joinerParam.add("id");
        joinerParam.add("time");

        StringJoiner joinerColumn = new StringJoiner(", ");
        joinerColumn.add("id");
        joinerColumn.add("time");

        List<String> name = new ArrayList<>();

        if (!measurements.get(0).getAnalogMeas().isEmpty()) {
            for (String columnName : measurements.get(0).getAnalogMeas().stream().map(AnalogMeas::getName).toList()) {
                namedParameterJdbcTemplate.update("ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + columnName + " DOUBLE",
                        Map.of());
                joinerColumn.add(columnName);
                joinerParam.add(columnName);
                name.add(columnName);
            }
        }
        if (!measurements.get(0).getDigitalMeas().isEmpty()) {
            for (String columnName : measurements.get(0).getDigitalMeas().stream().map(DigitalMeas::getName).toList()) {
                namedParameterJdbcTemplate.update("ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + columnName + " BOOLEAN",
                        Map.of());
                joinerColumn.add(columnName);
                joinerParam.add(columnName);
                name.add(columnName);
            }

        }
        if (!measurements.get(0).getRmsMeas().isEmpty()) {
            for (String columnName : measurements.get(0).getRmsMeas().stream().map(AnalogMeas::getName).toList()) {
                namedParameterJdbcTemplate.update("ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + columnName + " DOUBLE",
                        Map.of());
                joinerColumn.add(columnName);
                joinerParam.add(columnName);
                name.add(columnName);
            }
        }

        String columQuery = joinerColumn.toString();
        String paramQuery = joinerParam.toString();

        List<Map<String, Object>> meas_out = new ArrayList<>(measurements.size());
        AtomicInteger id = new AtomicInteger(1);
        measurements.forEach(m -> {
            Map<String, Object> meas = new HashMap<>();
            meas.put("id", id.getAndIncrement());
            meas.put("time", m.getTime());
            for (int i = 0; i < m.getAnalogMeas().size(); i++) {
                meas.put(m.getAnalogMeas().get(i).getName(), m.getAnalogMeas().get(i).getVal());
            }
            for (int i = 0; i < m.getDigitalMeas().size(); i++) {
                meas.put(m.getDigitalMeas().get(i).getName(), m.getDigitalMeas().get(i).isVal());
            }
            for (int i = 0; i < m.getRmsMeas().size(); i++) {
                meas.put(m.getRmsMeas().get(i).getName(), m.getRmsMeas().get(i).getVal());
            }
            meas_out.add(meas);
        });


        namedParameterJdbcTemplate.batchUpdate(
                "INSERT INTO " + table + " " +
                        "( " + columQuery + " ) " +
                        "SETTINGS async_insert=1, wait_for_async_insert=0 " +
                        "VALUES ( " + paramQuery + " ) ",
                SqlParameterSourceUtils.createBatch(meas_out)
        );

        log.info("data save in db, column {}", name.size());

    }

    public List<String> getMeasNames() {
        List<String> names = namedParameterJdbcTemplate.queryForList(
                "select COLUMNS.COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS " +
                        "where COLUMNS.TABLE_SCHEMA like :schema " +
                        "and COLUMNS.TABLE_NAME like :name ",
                Map.of("schema", table.split("\\.")[0],
                        "name", table.split("\\.")[1]),
                String.class);
        names.remove("time");
        names.remove("id");
        return names;
    }


    public List<MeasList> getMeasByNames(List<String> names, int start, int end) {
        StringJoiner joinerColumn = new StringJoiner(", ");
        joinerColumn.add("time");
        names.forEach(joinerColumn::add);

        return namedParameterJdbcTemplate.query(
                "select " + joinerColumn + " from " + table + " " +
                        "where id>=:start and id<=:end " +
                        "order by id",
                Map.of("start", start,
                        "end", end),
                new MeasurementMapper(names));
    }

    public List<ThreeMeasData> getThreeMeas(String phA, String phB, String phC) {
        StringJoiner joinerColumn = new StringJoiner(", ");
        joinerColumn.add("time");
        joinerColumn.add(phA);
        joinerColumn.add(phB);
        joinerColumn.add(phC);

        return namedParameterJdbcTemplate.query(
                "select " + joinerColumn + " from " + table,
                new ThreeMeasMapper(phA, phB, phC));
    }

    public MetaInf getMetaInf() {
        return JsonParser.parseData(namedParameterJdbcTemplate.queryForObject(
                        "SELECT comment FROM system.tables WHERE tables.name like :name and comment != ''",
                        Map.of("name", table.split("\\.")[1]), String.class),
                MetaInf.class);
    }

    private class MeasurementMapper implements RowMapper<MeasList> {

        private final List<String> names;

        public MeasurementMapper(List<String> names) {
            this.names = names;
        }

        @Override
        public MeasList mapRow(ResultSet rs, int rowNum) throws SQLException {
            MeasList measList = new MeasList();
            measList.setTime(rs.getDouble("time"));
            for (String name : names) {
                if (name.endsWith("_" + digitalSuffix)) {
                    measList.getDmeas().add(new DigitalMeas(name, rs.getBoolean(name)));
                } else {
                    measList.getMeas().add(new AnalogMeas(name, rs.getDouble(name)));
                }
            }
            return measList;
        }
    }

    private class ThreeMeasMapper implements RowMapper<ThreeMeasData> {

        private String phA;
        private String phB;
        private String phC;

        public ThreeMeasMapper(String phA, String phB, String phC) {
            this.phA = phA;
            this.phB = phB;
            this.phC = phC;
        }

        @Override
        public ThreeMeasData mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ThreeMeasData(
                    rs.getDouble("time"),
                    rs.getDouble(phA),
                    rs.getDouble(phB),
                    rs.getDouble(phC)
            );
        }
    }

}
