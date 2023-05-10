package ru.mpei.parser.model.comtrade;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnalogCfg {
    private int channelNumber;
    private String channelId;
    private String phaseId;
    private String component;
    private String unit;
    private double a;
    private double b;
    private double skew;
    private int min;
    private int max;
    private double primary;
    private double secondary;
    private Value value;

    public enum Value {
        P, S
    }
}
