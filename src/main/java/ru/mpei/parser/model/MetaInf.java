package ru.mpei.parser.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetaInf {
    private int n = 0;
    private double freq = 50;
    private String name = "empty";
    private String file1Name = "empty";
    private String file2Name = "empty";
    private double digital = -1;
    private double analog = -1;
    private String timeStart = "empty";
    private String timeEnd = "empty";
    private String type = "empty";

    public MetaInf(int n, double freq) {
        this.n = n;
        this.freq = freq;
    }
}
