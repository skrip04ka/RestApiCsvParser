package ru.mpei.parser.dto.comtrade.cfg;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamplingCfg {
    private double samplingFreq;
    private int lastNumber;
}
