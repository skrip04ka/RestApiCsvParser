package ru.mpei.parser.dto.comtrade.cfg;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
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
