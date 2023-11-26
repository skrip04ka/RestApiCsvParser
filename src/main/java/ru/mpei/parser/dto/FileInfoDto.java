package ru.mpei.parser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mpei.parser.model.FileType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDto {
    private UUID id;
    private int n;
    private double freq;
    private String name;
    private int digitalNumber;
    private int analogNumber;
    private String timeStart;
    private String timeEnd;
    private FileType type;

    public FileInfoDto(int n, double freq) {
        this.n = n;
        this.freq = freq;
    }
}
