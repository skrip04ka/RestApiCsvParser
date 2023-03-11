package ru.mpei.parser.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "measurements")
@NoArgsConstructor
public class Measurement {

    public Measurement(double time, double ia, double ib, double ic) {
        this.time = time;
        this.ia = ia;
        this.ib = ib;
        this.ic = ic;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private double time;
    @Column
    private double ia;
    @Column
    private double ib;
    @Column
    private double ic;

}
