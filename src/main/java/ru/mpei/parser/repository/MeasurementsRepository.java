package ru.mpei.parser.repository;

import ru.mpei.parser.model.Measurements;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.MeasList;
import ru.mpei.parser.model.measurement.ThreeMeasData;

import java.util.List;

public interface MeasurementsRepository {
    void saveMeas(List<Measurements> measurements, MetaInf metaInf);

    List<String> getMeasNames();

    List<MeasList> getMeasByNames(List<String> names, int start, int end);

    List<ThreeMeasData> getThreeMeas(String phA, String phB, String phC);

    MetaInf getMetaInf();
}
