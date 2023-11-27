package ru.mpei.parser.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mpei.parser.model.enums.FileType;

import java.util.List;
import java.util.UUID;

@Data
@Table(name = "files")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class File {
    @Id
    private UUID id;

    @Column
    private String name;

    @Column(nullable = false)
    private Integer n;

    @Column(nullable = false)
    private Double freq;

    @Column
    private Integer digitalNumber;

    @Column
    private Integer analogNumber;

    @Column
    private String timeStart;

    @Column
    private String timeEnd;

    @Enumerated(EnumType.STRING)
    private FileType type;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private List<Measurement> measurements;
}
