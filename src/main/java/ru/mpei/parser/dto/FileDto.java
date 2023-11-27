package ru.mpei.parser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mpei.parser.model.enums.FileType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private UUID id;
    private int n;
    private double freq;
    private String name;
    private Integer digitalNumber;
    private Integer analogNumber;
    private String timeStart;
    private String timeEnd;
    private FileType type;
}
