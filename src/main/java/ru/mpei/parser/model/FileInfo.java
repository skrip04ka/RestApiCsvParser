package ru.mpei.parser.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Table(name = "file_info")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {
    @Id
    private UUID id;
    private String name;
    private int n;
    private double freq;
    private int digitalNumber;
    private int analogNumber;
    private String timeStart;
    private String timeEnd;
    @Enumerated(EnumType.STRING)
    private FileType type;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_info_id")
    private List<Measurement> measurements;
}
