package ru.mpei.parser.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FileInfo;
import ru.mpei.parser.model.dto.NamedMeas;
import ru.mpei.parser.repository.MeasurementsRepository;

import java.util.*;

@Repository
@Slf4j
public class MeasurementRepositoryImpl implements MeasurementsRepository {
    @Value("${repository.querySize}")
    private int querySize = 15000;
    private final NamedParameterJdbcTemplate template;

    public MeasurementRepositoryImpl(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void saveMeas(Map<String, List<Double>> valuesByName, MetaInf metaInf) {
        KeyHolder holder = new GeneratedKeyHolder();
        template.update("insert into meta_inf (name, n, freq, digital, analog, time_start, time_end, type) " +
                        "values (:name, :n, :freq, :digital, :analog, :time_start, :time_end, :type)",
                new MapSqlParameterSource()
                        .addValue("name", metaInf.getName())
                        .addValue("n", metaInf.getN())
                        .addValue("freq", metaInf.getFreq())
                        .addValue("digital", metaInf.getDigital())
                        .addValue("analog", metaInf.getAnalog())
                        .addValue("time_start", metaInf.getTimeStart())
                        .addValue("time_end", metaInf.getTimeEnd())
                        .addValue("type", metaInf.getType()),
                holder, new String[]{"meta_id"});

        long metaId = Objects.requireNonNull(holder.getKey()).longValue();
        Map<String, Object>[] firstBath = new HashMap[valuesByName.size()];
        Map<String, Object>[] midlBaths = null;
        Map<String, Object>[] lastBath = null;

        int size = valuesByName.get("time").size();
        int end = Math.min(querySize, size);

        int i = 0;
        for (String name : valuesByName.keySet()) {
            firstBath[i++] = new HashMap<>(Map.of("metaId", metaId,
                    "measName", name, "val", valuesByName.get(name).subList(0, end)));
        }
        log.info("first bath get with values size = {}", end);

        if (end < size) {
            int start = end;
            end = Math.min(end + querySize, size);

            midlBaths = new HashMap[valuesByName.size() * (size / querySize - 1)];
            i = 0;
            while (i < midlBaths.length) {
                for (String name : valuesByName.keySet()) {
                    midlBaths[i++] = new HashMap<>(Map.of("name", name, "metaId", metaId,
                            "val", valuesByName.get(name).subList(start, end)));
                }
                start = end;
                end = end + querySize;
            }

            log.info("midl bath get with size = {} and values size = {}", midlBaths.length, start - querySize);
            if (start < size) {
                lastBath = new HashMap[valuesByName.size()];
                i = 0;
                for (String name : valuesByName.keySet()) {
                    lastBath[i++] = new HashMap<>(Map.of("name", name, "metaId", metaId,
                            "val", valuesByName.get(name).subList(start, size)));
                }
                log.info("last bath get with values size = {}", size - start);
            }
        }

        template.batchUpdate("insert into measurements (meta_id, meas_name, values) " +
                " values (:metaId, :measName, ARRAY [ :val ]) ", firstBath);
        if (midlBaths != null) {
            template.batchUpdate("update measurements set values = values || ARRAY [ :val ] " +
                    " where meas_id = (select meas_id from measurements " +
                    " where meta_id = :metaId and meas_name = :name )", midlBaths);
            if (lastBath != null) {
                template.batchUpdate("update measurements set values = values || ARRAY [ :val ] " +
                        " where meas_id = (select meas_id from measurements " +
                        " where meta_id = :metaId and meas_name = :name )", lastBath);
            }
        }
        log.info("saved with querySize = {} and size = {}", querySize, size);

    }

    @Override
    public List<String> getMeasNames(long id) {
        return template.queryForList("select meas_name from measurements " +
                        " join meta_inf mi on mi.meta_id = measurements.meta_id " +
                        " where mi.meta_id = :id and meas_name != 'time'",
                Map.of("id", id), String.class);
    }

    @Override
    public List<NamedMeas> getMeasByNamesAndRange(long id, List<String> names, int start, int end) {
        return template.query("select meas_name, values[ :start : :end ] from measurements " +
                        " join meta_inf mi on mi.meta_id = measurements.meta_id " +
                        " where (meas_name in ( :names ) or meas_name = 'time') and mi.meta_id = :id ",
                Map.of("id", id, "start", start, "end", end, "names", names),
                (rs, rowNum) -> new NamedMeas(rs.getString("meas_name"),
                        (Double[]) rs.getArray("values").getArray()));
    }

    @Override
    public List<NamedMeas> getMeasByNames(long id, List<String> names) {
        return template.query("select meas_name, values from measurements " +
                        " join meta_inf mi on mi.meta_id = measurements.meta_id " +
                        " where (meas_name in ( :names ) or meas_name = 'time') and mi.meta_id = :id ",
                Map.of("id", id, "names", names),
                (rs, rowNum) -> new NamedMeas(rs.getString("meas_name"),
                        (Double[]) rs.getArray("values").getArray()));
    }

    @Override
    public Optional<MetaInf> getMetaInf(long id) {
        return Optional.ofNullable(template.queryForObject(
                "select * from meta_inf where meta_id = :id ",
                Map.of("id", id),
                (rs, rowNum) -> {
                    MetaInf metaInf = new MetaInf(rs.getInt("n"), rs.getDouble("freq"));
                    metaInf.setName(rs.getString("name"));
                    metaInf.setTimeStart(rs.getString("time_start"));
                    metaInf.setTimeEnd(rs.getString("time_end"));
                    metaInf.setType(rs.getString("type"));
                    metaInf.setAnalog(rs.getInt("analog"));
                    metaInf.setDigital(rs.getInt("digital"));
                    return metaInf;
                }));
    }

    @Override
    public List<FileInfo> getFilesInfo() {
        return template.query("select meta_id, name from meta_inf",
                Map.of(), (rs, rowNum) -> new FileInfo(
                        rs.getLong("meta_id"),
                        rs.getString("name")));
    }
}
