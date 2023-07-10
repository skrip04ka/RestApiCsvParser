package ru.mpei.parser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FileInfo;
import ru.mpei.parser.model.dto.MeasByName;
import ru.mpei.parser.model.dto.MeasList;
import ru.mpei.parser.model.measurement.AnalogMeas;
import ru.mpei.parser.model.measurement.DigitalMeas;
import ru.mpei.parser.repository.MeasurementsRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RepositoryService {
    private final MeasurementsRepository measurementsRepository;
    @Value("${parser.digitalSuffix}")
    private String digitalSuffix = "BOOL";

    @Autowired
    public RepositoryService(MeasurementsRepository measurementsRepository) {
        this.measurementsRepository = measurementsRepository;
    }

    public List<MeasList> getMeasByName(long id, List<String> names, int start, int end) {
        if (start - end > 60000) end = start + 60000;

        List<MeasByName> measByNames = measurementsRepository.getMeasByNames(id, names, start, end);

        if (measByNames.isEmpty()) return null;

        List<MeasList> res = new ArrayList<>();
        for (MeasByName m : measByNames) {
            String[] namesVal = m.getNames();
            double[] val = m.getValues();

            ArrayList<DigitalMeas> digital = new ArrayList<>();
            ArrayList<AnalogMeas> analog = new ArrayList<>();

            for (int i = 0; i < namesVal.length; i++) {
                if (namesVal[i].endsWith("_" + digitalSuffix)) {
                    digital.add(new DigitalMeas(namesVal[i], val[i] == 1.0));
                } else {
                    analog.add(new AnalogMeas(namesVal[i], val[i]));
                }
            }
            res.add(new MeasList(m.getTime(), analog, digital));
        }

        return res;
    }

    public MetaInf getMetaInf(long id) {
        return measurementsRepository.getMetaInf(id).orElseThrow();
    }

    public List<String> getMeasName(long id) {
        return measurementsRepository.getMeasNames(id).orElseThrow();
    }

    public List<FileInfo> getFilesInfo() {
        return measurementsRepository.getFilesInfo();
    }
}
