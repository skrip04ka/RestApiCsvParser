package ru.mpei.parser.repository;

import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FileInfo;
import ru.mpei.parser.model.dto.NamedMeas;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MeasurementsRepository {
    void saveMeas(Map<String, List<Double>> valuesByName, MetaInf metaInf);

    List<String> getMeasNames(long id);

    List<NamedMeas> getMeasByNamesAndRange(long id, List<String> names, int start, int end);

    List<NamedMeas> getMeasByNames(long id, List<String> names);

    Optional<MetaInf> getMetaInf(long id);

    List<FileInfo> getFilesInfo();
}
