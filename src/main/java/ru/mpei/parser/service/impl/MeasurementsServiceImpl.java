package ru.mpei.parser.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.dto.FileInfo;
import ru.mpei.parser.model.dto.NamedMeas;
import ru.mpei.parser.model.dto.MeasList;
import ru.mpei.parser.model.measurement.AnalogMeas;
import ru.mpei.parser.model.measurement.DigitalMeas;
import ru.mpei.parser.repository.MeasurementsRepository;
import ru.mpei.parser.service.MeasurementsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MeasurementsServiceImpl implements MeasurementsService {
    private final MeasurementsRepository measurementsRepository;
    @Value("${parser.digitalSuffix}")
    private String digitalSuffix = "BOOL";

    @Autowired
    public MeasurementsServiceImpl(MeasurementsRepository measurementsRepository) {
        this.measurementsRepository = measurementsRepository;
    }

    @Override public List<MeasList> getMeasByName(long id, List<String> names, int start, int end) {
        if (start - end > 60000) end = start + 60000;

        List<NamedMeas> namedMeas = measurementsRepository.getMeasByNamesAndRange(id, names, start, end);

        if (namedMeas.isEmpty()) return null;

        Double[] time = namedMeas.stream().filter(m -> m.getName().equals("time"))
                .findFirst().orElseThrow().getValues();

        Map<String, Double[]> map = namedMeas.stream().filter(m -> !m.getName().equals("time"))
                .collect(Collectors.toMap(NamedMeas::getName, NamedMeas::getValues));

        List<MeasList> res = new ArrayList<>();

        for (int i = 0; i < time.length; i++) {

            ArrayList<DigitalMeas> digital = new ArrayList<>();
            ArrayList<AnalogMeas> analog = new ArrayList<>();

            for (String name: map.keySet()) {
                if (name.endsWith("_" + digitalSuffix)) {
                    digital.add(new DigitalMeas(name, map.get(name)[i] == 1.0));
                } else {
                    analog.add(new AnalogMeas(name, map.get(name)[i]));
                }
            }
            res.add(new MeasList(time[i], analog, digital));
        }

        return res;
    }

    @Override public MetaInf getMetaInf(long id) {
        return measurementsRepository.getMetaInf(id).orElseThrow();
    }

    @Override public List<String> getMeasName(long id) {
        return measurementsRepository.getMeasNames(id);
    }

    @Override public List<FileInfo> getFilesInfo() {
        return measurementsRepository.getFilesInfo();
    }
}
