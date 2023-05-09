package ru.mpei.parser.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.mpei.parser.model.CfgData;
import ru.mpei.parser.model.Measurements;
import ru.mpei.parser.model.MetaInf;
import ru.mpei.parser.model.comtrade.AnalogCfg;
import ru.mpei.parser.model.comtrade.DigitalCfg;
import ru.mpei.parser.model.comtrade.SamplingCfg;
import ru.mpei.parser.model.measurement.AnalogMeas;
import ru.mpei.parser.model.measurement.DigitalMeas;
import ru.mpei.parser.repository.ClickHouseRepository;
import ru.mpei.parser.util.ParserUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class ComtradeService {

    @Value("${parser.comtrade.charSetName}")
    private String charSetName = "Windows-1251";
    @Value("${parser.digitalSuffix}")
    private String digitalSuffix = "BOOL";
    private final ClickHouseRepository clickHouseRepository;
    private final FourierFilterService filterService;

//    private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SSSSSS");

    @Autowired
    public ComtradeService(ClickHouseRepository clickHouseRepository, FourierFilterService filterService) {
        this.clickHouseRepository = clickHouseRepository;
        this.filterService = filterService;
    }

    @SneakyThrows
    public void parseFile(MultipartFile cfg, MultipartFile dat) {
        CfgData cfgData;
        List<Measurements> measurements = readData(dat.getInputStream(), cfgData = readCfg(cfg.getInputStream()));

        log.info("read data complete");
        int N = filterService.rmsByPhase(measurements, cfgData.getFreq());
        log.info("rms calculate complete with N={}", N);

        MetaInf metaInf = new MetaInf(N, cfgData.getFreq());
        metaInf.setFile1Name(cfg.getOriginalFilename());
        metaInf.setFile2Name(dat.getOriginalFilename());
        metaInf.setAnalog(cfgData.getAnalogChannels().size());
        metaInf.setDigital(cfgData.getDigitalChannels().size());
        metaInf.setType("COMTRADE " + cfgData.getFileType().name());
        metaInf.setTimeStart(cfgData.getDateStart());
        metaInf.setTimeEnd(cfgData.getDateEnd());

        clickHouseRepository.saveMeas(measurements, metaInf);
    }

    @SneakyThrows
    private CfgData readCfg(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charSetName));
        CfgData cfgData = new CfgData();

        cfgData.setName(bufferedReader.readLine());
        String[] line = bufferedReader.readLine().split(",");

        int aCount;
        int dCount;

        String value = line[1];
        if (value.contains("A")) {
            aCount = Integer.parseInt(value.substring(0, value.length() - 1));
            value = line[2];
            dCount = Integer.parseInt(value.substring(0, value.length() - 1));
        } else {
            dCount = Integer.parseInt(value.substring(0, value.length() - 1));
            value = line[2];
            aCount = Integer.parseInt(value.substring(0, value.length() - 1));
        }

        List<AnalogCfg> analogCfgList = new ArrayList<>();
        List<DigitalCfg> digitalCfgList = new ArrayList<>();
        Map<Integer, CfgData.DataType> dataTypes = new HashMap<>(aCount + dCount);

        for (int i = 0; i < aCount + dCount; i++) {
            line = bufferedReader.readLine().split(",");
            if (line.length == 13) {
                analogCfgList.add(getAnalogCfg(line));
                dataTypes.put(i, CfgData.DataType.A);
            } else if (line.length == 5) {
                digitalCfgList.add(getDigitalCfg(line));
                dataTypes.put(i, CfgData.DataType.D);
            } else {
                log.error("unsupported line type {}", Arrays.toString(line));
            }
        }
        cfgData.setAnalogChannels(analogCfgList);
        cfgData.setDigitalChannels(digitalCfgList);
        cfgData.setDataTypes(dataTypes);
        cfgData.setFreq(Double.parseDouble(bufferedReader.readLine()));

        int sCount = Integer.parseInt(bufferedReader.readLine());
        List<SamplingCfg> samplingCfgList = new ArrayList<>();
        for (int i = 0; i < sCount; i++) {
            line = bufferedReader.readLine().split(",");
            SamplingCfg samplingCfg = new SamplingCfg();
            samplingCfg.setSamplingFreq(Double.parseDouble(line[0]));
            samplingCfg.setLastNumber(Integer.parseInt(line[1]));
            samplingCfgList.add(samplingCfg);
        }
        cfgData.setSamplingsFreq(samplingCfgList);
        cfgData.setDateStart(bufferedReader.readLine());
        cfgData.setDateEnd(bufferedReader.readLine());

        cfgData.setFileType(bufferedReader.readLine().contains("BINARY") ? CfgData.FileType.BINARY : CfgData.FileType.ASCII);

        bufferedReader.close();
        return cfgData;
    }

    private AnalogCfg getAnalogCfg(String[] line) {
        AnalogCfg analogCfg = new AnalogCfg();
        analogCfg.setChannelNumber(Integer.parseInt(line[0]));
        analogCfg.setChannelId(Integer.parseInt(line[0]) + "_" + ParserUtil.toCorrectStr(line[1]));
        analogCfg.setPhaseId(line[2]);
        analogCfg.setComponent(line[3]);
        analogCfg.setUnit(line[4]);
        analogCfg.setA(Double.parseDouble(line[5]));
        analogCfg.setB(Double.parseDouble(line[6]));
        analogCfg.setSkew(Double.parseDouble(line[7]));
        analogCfg.setMin(Integer.parseInt(line[8]));
        analogCfg.setMax(Integer.parseInt(line[9]));
        analogCfg.setPrimary(Double.parseDouble(line[10]));
        analogCfg.setSecondary(Double.parseDouble(line[11]));
        analogCfg.setValue(line[12].contains("S") ? AnalogCfg.Value.S : AnalogCfg.Value.P);
        return analogCfg;
    }


    private DigitalCfg getDigitalCfg(String[] line) {
        DigitalCfg digitalCfg = new DigitalCfg();
        digitalCfg.setChannelNumber(Integer.parseInt(line[0]));
        digitalCfg.setChannelId(line[0] + "_" + ParserUtil.toCorrectStr(line[1]) + "_" + digitalSuffix);
        digitalCfg.setNormalState(!line[2].equals("") ? Integer.parseInt(line[2]) : 0);
        return digitalCfg;
    }


    private List<Measurements> readData(InputStream inputStream, CfgData cfg) {
        return switch (cfg.getFileType()) {
            case ASCII -> asciiRead(inputStream, cfg);
            case BINARY -> binaryRead(inputStream, cfg);
        };
    }

    @SneakyThrows
    private List<Measurements> binaryRead(InputStream inputStream, CfgData cfg) {
        byte[] bytes = inputStream.readAllBytes();
        inputStream.close();

        List<Measurements> measurements = new ArrayList<>();
        int i = 0;

        while (i < bytes.length) {
            Measurements meas = new Measurements();
            i = i + 4;
            meas.setTime(((double) ParserUtil.bArrTo32UInt(bytes, i)) / 1_000_000);
            i = i + 4;

            List<AnalogMeas> analogMeas = new ArrayList<>();
            for (AnalogCfg ac : cfg.getAnalogChannels()) {
                analogMeas.add(new AnalogMeas(
                        ac.getChannelId(),
                        (ParserUtil.bArrTo16Int(bytes, i) * ac.getA() + ac.getB()) *
                                (ac.getValue() == AnalogCfg.Value.S ? ac.getPrimary() / ac.getSecondary() : 1)
                ));
                i = i + 2;
            }
            meas.setAnalogMeas(analogMeas);

            List<DigitalMeas> digitalMeas = new ArrayList<>();
            for (int j = 0; j < Math.ceilDiv(cfg.getDigitalChannels().size(), 8); j++) {
                List<Boolean> arr = ParserUtil.bArrTo8Bit(bytes, i);
                for (int k = 0; k < 8; k++) {
                    if (cfg.getDigitalChannels().size() < j * 8 + k) break;
                    digitalMeas.add(new DigitalMeas(cfg.getDigitalChannels().get(j * 8 + k).getChannelId(), arr.get(k)));
                }
                i++;
            }
            meas.setDigitalMeas(digitalMeas);

            measurements.add(meas);
        }


        return measurements;
    }


    @SneakyThrows
    private List<Measurements> asciiRead(InputStream inputStream, CfgData cfg) {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8.newDecoder()));

        String line;
        List<Measurements> measurements = new ArrayList<>();


        while ((line = bufferedReader.readLine()) != null) {
            Measurements meas = new Measurements();
            String[] l = line.split(",");
            int offset = 0;

            offset++;
            meas.setTime(Double.parseDouble(l[offset]) / 1_000_000);

            offset++;

            for (int i = 0; i < cfg.getAnalogChannels().size(); i++) {
                AnalogCfg ac = cfg.getAnalogChannels().get(i);
                meas.getAnalogMeas().add(new AnalogMeas(ac.getChannelId(),
                        (Double.parseDouble(l[i + offset]) * ac.getA() + ac.getB()) *
                                (ac.getValue() == AnalogCfg.Value.S ? ac.getPrimary() / ac.getSecondary() : 1)));
            }
            offset = offset + cfg.getAnalogChannels().size();
            for (int i = 0; i < cfg.getDigitalChannels().size(); i++) {
                DigitalCfg ac = cfg.getDigitalChannels().get(i);
                meas.getDigitalMeas().add(new DigitalMeas(ac.getChannelId(), Integer.parseInt(l[i + offset]) == 1));
            }
            measurements.add(meas);
        }

        bufferedReader.close();
        return measurements;
    }

}
