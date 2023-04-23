package ru.mpei.parser.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.mpei.parser.model.MeasurementRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class ClickHouseNative {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ClickHouseNative(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public void save(MeasurementRecord measurement) {
        namedParameterJdbcTemplate.update(
                "insert into measurement.test (timestamp, IA, IB, IC ) " +
                        "SETTINGS async_insert=1, wait_for_async_insert=0 " +
                        "values (:timestamp, :ia, :ib, :ic) ",
                new MapSqlParameterSource()
                        .addValue("timestamp", measurement.timestamp())
                        .addValue("ia", measurement.ia())
                        .addValue("ib", measurement.ib())
                        .addValue("ic", measurement.ic()));
    }

    public List<MeasurementRecord> findAll() {
        return namedParameterJdbcTemplate.query(
                "select * from measurement.test",
                new MeasurementMapper());
    }

    private static class MeasurementMapper implements RowMapper<MeasurementRecord> {

        @Override
        public MeasurementRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new MeasurementRecord(
                    rs.getDouble("timestamp"),
                    rs.getDouble("ia"),
                    rs.getDouble("ib"),
                    rs.getDouble("ic")
            );
        }
    }
}
