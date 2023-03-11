package ru.mpei.comtrade.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.mpei.comtrade.model.Measurement;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class MeasurementsRepository {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    private void init() {
        em.setFlushMode(FlushModeType.COMMIT);
    }

    @Transactional
    public void save(Measurement m){
        if (m.getId() == 0) {
            em.persist(m);
        } else {
            em.merge(m);
        }
    }

    @Transactional
    public void flush(){
        em.flush();
    }

    @Transactional
    public List<Measurement> getMeasByIdDiapason(long startIndex, long endIndex){
        return em.createQuery("select m from Measurement m where m.id >= :startId and m.id <= :endId",
                        Measurement.class)
                .setParameter("startId", startIndex)
                .setParameter("endId", endIndex)
                .getResultList();
    }

    @Transactional
    public Long getLastIndex(){

        return em.createQuery("select max(m.id) from Measurement m", Long.class).getSingleResult();
    }


}
