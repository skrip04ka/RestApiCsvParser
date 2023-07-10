package ru.mpei.parser.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.mpei.parser.model.Measurements;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FileInfo;
import ru.mpei.parser.model.dto.MeasByName;
import ru.mpei.parser.model.measurement.AnalogMeas;
import ru.mpei.parser.model.measurement.DigitalMeas;
import ru.mpei.parser.model.measurement.ThreeMeasData;
import ru.mpei.parser.repository.MeasurementsRepository;
import ru.mpei.parser.util.JsonParser;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MeasurementsRepositoryImpl implements MeasurementsRepository {
    private final NamedParameterJdbcTemplate template;
    @Value("${clickhouse.table.name}")
    private String table;
    @Value("${clickhouse.table.meta}")
    private String meta;

    @Autowired
    public MeasurementsRepositoryImpl(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void saveMeas(List<Measurements> measurements, MetaInf metaInf) {
        Map<String, Object>[] bath = new HashMap[measurements.size()];
        List<String> names = new ArrayList<>();
        long id = getId();
        int i = 0;
        for (Measurements m : measurements) {
            Map<String, Double> map = new HashMap<>();
            for (AnalogMeas am : m.getAnalogMeas()) {
                map.put(am.getName(), am.getVal());
            }
            for (AnalogMeas am : m.getRmsMeas()) {
                map.put(am.getName(), am.getVal());
            }
            for (DigitalMeas am : m.getDigitalMeas()) {
                map.put(am.getName(), am.isVal() ? 1.0 : 0.0);
            }
            if (names.isEmpty()) names.addAll(map.keySet());
            bath[i] = new HashMap<>(Map.of("id", id, "time", m.getTime(), "val", map, "timeId", i));
            i++;
        }

        template.batchUpdate("INSERT INTO " + table + " VALUES ( :id, :timeId, :time, :val )", bath);
        template.update("INSERT INTO " + meta + " VALUES ( :id, :name, :meta, [ :names ] )",
                Map.of("id", id, "name", metaInf.getName(),
                        "meta", JsonParser.dataToString(metaInf), "names", names));
    }

    private long getId() {
        Long id = template.queryForObject("select max(id) from " + table,
                Map.of(), Long.class);
        return id == null ? 0 : id + 1;
    }

    @Override
    public Optional<List<String>> getMeasNames(long id) {
        String[] names = template.queryForObject("select names from " + meta +
                        " where (id = :id)",
                Map.of("id", id),
                String[].class);

        if (names == null) return Optional.empty();
        return Optional.of(Arrays.stream(names).filter(v -> !v.equals("time")).collect(Collectors.toList()));
    }


    @Override
    public List<MeasByName> getMeasByNames(long id, List<String> names, int start, int end) {
        return template.query("select time, groupArray(values[arrayJoin(names)]) as values, " +
                        " [ :names ] as names from " + table +
                        " where (id = :id) and time_id >= :start and time_id < :end " +
                        "group by time order by time",
                Map.of("start", start, "end", end, "names", names, "id", id),
                (rs, rowNum) -> new MeasByName(rs.getDouble("time"),
                        (String[]) rs.getArray("names").getArray(),
                        (double[]) rs.getArray("values").getArray()));
    }


    @Override
    public List<ThreeMeasData> getThreeMeas(long id, String phA, String phB, String phC) {
        return template.query("select time, groupArray(values[arrayJoin(names)]) as values, " +
                        " [ :names ] as names from " + table +
                        " where (id = :id) group by time order by time",
                Map.of("names", List.of(phA, phB, phC), "id", id),
                (rs, rowNum) -> ThreeMeasData.create(rs.getDouble("time"),
                        (String[]) rs.getArray("names").getArray(),
                        (double[]) rs.getArray("values").getArray(), phA, phB, phC));
    }

    @Override
    public Optional<MetaInf> getMetaInf(long id) {
        return Optional.ofNullable(JsonParser.parseData(template.queryForObject(
                        "SELECT meta FROM " + meta + " WHERE id = :id",
                        Map.of("id", id), String.class),
                MetaInf.class));
    }

    @Override
    public List<FileInfo> getFilesInfo() {
        return template.query("SELECT id, name FROM " + meta + " order by id ",
                Map.of(),
                (rs, rowNum) ->
                        new FileInfo(rs.getLong("id"), rs.getString("name")));
    }
}
