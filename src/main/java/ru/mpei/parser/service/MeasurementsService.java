package ru.mpei.parser.service;

import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FileInfo;
import ru.mpei.parser.model.dto.MeasList;

import java.util.List;

public interface MeasurementsService {
    List<MeasList> getMeasByName(long id, List<String> names, int start, int end);

    MetaInf getMetaInf(long id);

    List<String> getMeasName(long id);

    List<FileInfo> getFilesInfo();
}
