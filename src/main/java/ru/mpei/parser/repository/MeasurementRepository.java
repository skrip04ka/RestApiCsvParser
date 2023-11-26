package ru.mpei.parser.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.mpei.parser.dto.view.MeasurementInfoView;
import ru.mpei.parser.mapper.MeasurementMapper;
import ru.mpei.parser.model.File;
import ru.mpei.parser.model.Measurement;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class MeasurementRepository {

    @PersistenceContext
    private EntityManager em;

    public void saveFile(File file) {
        em.persist(file);
    }


    public List<File> getFiles() {
        return em.createQuery("select f from File f", File.class)
                .getResultList();
    }

    public Optional<File> getFileById(UUID fileId) {
        return Optional.ofNullable(em.find(File.class, fileId));
    }

    public List<Measurement> getMeasurementsByFileIdAndSignalNumbers(UUID fileId, List<Integer> signalNumbers) {
        return em.createQuery("select m from Measurement m " +
                                "where m.key.fileId = :fileId " +
                                "and m.key.signalNumber in :signalNumbers ",
                        Measurement.class)
                .setParameter("fileId", fileId)
                .setParameter("signalNumbers", signalNumbers)
                .getResultList();
    }

    public List<MeasurementInfoView> getMeasurementsInfo(UUID fileId) {
        return em.createQuery("select m.key, m.name, m.number from Measurement m " +
                        "where m.key.fileId = :fileId order by m.key.signalNumber", Tuple.class)
                .setParameter("fileId", fileId)
                .getResultList()
                .stream()
                .map(t -> MeasurementInfoView.builder()
                        .key(MeasurementMapper.mapToKeyView((Measurement.Key) t.get(0)))
                        .name((String) t.get(1))
                        .number((Integer) t.get(2))
                        .build())
                .toList();
    }

    public void deleteFile(UUID fileId) {
        em.createQuery("delete File f where f.id = :fileId")
                .setParameter("fileId", fileId)
                .executeUpdate();
    }
}
