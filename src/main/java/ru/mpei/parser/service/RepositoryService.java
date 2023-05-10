package ru.mpei.parser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.MeasList;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.repository.ClickHouseRepository;

import java.util.List;

@Service
public class RepositoryService {
    private final ClickHouseRepository clickHouseRepository;

    @Autowired
    public RepositoryService(ClickHouseRepository clickHouseRepository) {
        this.clickHouseRepository = clickHouseRepository;
    }

    public List<MeasList> getMeasByName(List<String> names, int start, int end) {
        if (start - end > 60000) end = start + 60000;
        return clickHouseRepository.getMeasByNames(names, start, end);
    }

    public MetaInf getMetaInf() {
        return clickHouseRepository.getMetaInf();
    }

    public List<String> getMeasName() {
        return clickHouseRepository.getMeasNames();
    }
}
