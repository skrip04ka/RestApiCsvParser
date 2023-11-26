package ru.mpei.parser.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Table(name = "measurements")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {
    @EmbeddedId
    private Key key;
    private int number;
    private String name;

    @Convert(converter = ValuesConverter.class)
    private List<Double> values;

    @Data
    @Embeddable
    @NoArgsConstructor
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class Key implements Serializable {
        @Column(name = "file_info_id")
        private UUID fileInfoId;

        private int signalNumber;

        @Enumerated(EnumType.STRING)
        private SignalType type;
    }
}
