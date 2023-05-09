package ru.mpei.parser.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MetaInf {
    private int n = 0;
    private double freq = 50;
    private String file1Name = "";
    private String file2Name = "";
    private double digital = -1;
    private double analog = -1;
    private String timeStart = "";
    private String timeEnd = "";
    private String type = "";

    public MetaInf(int n, double freq) {
        this.n = n;
        this.freq = freq;
    }
}
