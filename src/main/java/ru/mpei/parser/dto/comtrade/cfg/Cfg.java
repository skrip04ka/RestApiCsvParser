package ru.mpei.parser.dto.comtrade.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Cfg {
    private String name;
    private List<AnalogCfg> analogChannels;
    private List<DigitalCfg> digitalChannels;
    private Map<Integer, ComtradeDataType> dataTypes;
    private double freq;
    private List<SamplingCfg> samplingsFreq;
    private String dateStart;
    private String dateEnd;
    private ComtradeFileType comtradeFileType;

    public enum ComtradeFileType {
        ASCII, BINARY
    }
    public enum ComtradeDataType {
        A, D
    }
}
