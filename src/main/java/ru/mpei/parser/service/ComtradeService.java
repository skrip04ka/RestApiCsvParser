package ru.mpei.parser.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mpei.parser.dto.FileInfoDto;
import ru.mpei.parser.dto.data.MeasData;
import ru.mpei.parser.dto.comtrade.cfg.AnalogCfg;
import ru.mpei.parser.dto.comtrade.cfg.Cfg;
import ru.mpei.parser.dto.comtrade.cfg.DigitalCfg;
import ru.mpei.parser.dto.comtrade.cfg.SamplingCfg;
import ru.mpei.parser.dto.data.AnalogMeasData;
import ru.mpei.parser.dto.data.DigitalMeasData;
import ru.mpei.parser.mapper.FileMapper;
import ru.mpei.parser.model.FileType;
import ru.mpei.parser.repository.MeasurementRepository;
import ru.mpei.parser.service.filter.RmsFilterService;
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

    private final MeasurementRepository measurementsRepository;
    private final RmsFilterService filterService;

    @Autowired
    public ComtradeService(MeasurementRepository measurementsRepository, RmsFilterService filterService) {
        this.measurementsRepository = measurementsRepository;
        this.filterService = filterService;
    }

    @SneakyThrows
    @Transactional
    public void parseFile(MultipartFile cfg, MultipartFile dat) {
        Cfg cfgData = readCfg(cfg.getInputStream());
        List<MeasData> measurements = readData(dat.getInputStream(), cfgData);

        log.info("read data complete");
        int N = filterService.calculate(measurements, cfgData.getFreq());
        log.info("rms calculate complete with N={}", N);

        FileInfoDto fileInfoDto = getFileInfoDto(cfg, N, cfgData);

        measurementsRepository.saveFile(FileMapper.mapToFileInfo(fileInfoDto, measurements));
    }

    private static FileInfoDto getFileInfoDto(MultipartFile cfg, int N, Cfg cfgData) {
        FileInfoDto fileInfoDto = new FileInfoDto(N, cfgData.getFreq());
        if (cfg.getOriginalFilename() != null)
            fileInfoDto.setName(cfg.getOriginalFilename().split("\\.cfg")[0]);
        fileInfoDto.setAnalogNumber(cfgData.getAnalogChannels().size());
        fileInfoDto.setDigitalNumber(cfgData.getDigitalChannels().size());
        fileInfoDto.setType(FileType.valueOf("COMTRADE_" + cfgData.getComtradeFileType()));
        fileInfoDto.setTimeStart(cfgData.getDateStart());
        fileInfoDto.setTimeEnd(cfgData.getDateEnd());
        return fileInfoDto;
    }

    @SneakyThrows
    private Cfg readCfg(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charSetName));
        Cfg cfg = new Cfg();

        cfg.setName(bufferedReader.readLine());
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
        Map<Integer, Cfg.ComtradeDataType> dataTypes = new HashMap<>(aCount + dCount);

        for (int i = 0; i < aCount + dCount; i++) {
            line = bufferedReader.readLine().split(",");
            if (line.length == 13) {
                analogCfgList.add(getAnalogCfg(line));
                dataTypes.put(i, Cfg.ComtradeDataType.A);
            } else if (line.length == 5) {
                digitalCfgList.add(getDigitalCfg(line));
                dataTypes.put(i, Cfg.ComtradeDataType.D);
            } else {
                log.error("unsupported line type {}", Arrays.toString(line));
            }
        }
        cfg.setAnalogChannels(analogCfgList);
        cfg.setDigitalChannels(digitalCfgList);
        cfg.setDataTypes(dataTypes);
        cfg.setFreq(Double.parseDouble(bufferedReader.readLine()));

        int sCount = Integer.parseInt(bufferedReader.readLine());
        List<SamplingCfg> samplingCfgList = new ArrayList<>();
        for (int i = 0; i < sCount; i++) {
            line = bufferedReader.readLine().split(",");
            SamplingCfg samplingCfg = new SamplingCfg();
            samplingCfg.setSamplingFreq(Double.parseDouble(line[0]));
            samplingCfg.setLastNumber(Integer.parseInt(line[1]));
            samplingCfgList.add(samplingCfg);
        }
        cfg.setSamplingsFreq(samplingCfgList);
        cfg.setDateStart(bufferedReader.readLine());
        cfg.setDateEnd(bufferedReader.readLine());

        cfg.setComtradeFileType(bufferedReader.readLine().contains("BINARY") ? Cfg.ComtradeFileType.BINARY : Cfg.ComtradeFileType.ASCII);

        bufferedReader.close();
        return cfg;
    }

    private AnalogCfg getAnalogCfg(String[] line) {
        return AnalogCfg.builder()
                .channelNumber(Integer.parseInt(line[0]))
                .channelId(line[1])
                .phaseId(line[2])
                .component(line[3])
                .unit(line[4])
                .a(Double.parseDouble(line[5]))
                .b(Double.parseDouble(line[6]))
                .skew(Double.parseDouble(line[7]))
                .min(Integer.parseInt(line[8]))
                .max(Integer.parseInt(line[9]))
                .primary(Double.parseDouble(line[10]))
                .secondary(Double.parseDouble(line[11]))
                .value(line[12].contains("S") ? AnalogCfg.Value.S : AnalogCfg.Value.P)
                .build();
    }

    private DigitalCfg getDigitalCfg(String[] line) {
        DigitalCfg digitalCfg = new DigitalCfg();
        digitalCfg.setChannelNumber(Integer.parseInt(line[0]));
        digitalCfg.setChannelId(line[1]);
        digitalCfg.setNormalState(!line[2].isEmpty() ? Integer.parseInt(line[2]) : 0);
        return digitalCfg;
    }


    private List<MeasData> readData(InputStream inputStream, Cfg cfg) {
        return switch (cfg.getComtradeFileType()) {
            case ASCII -> asciiRead(inputStream, cfg);
            case BINARY -> binaryRead(inputStream, cfg);
        };
    }

    @SneakyThrows
    private List<MeasData> binaryRead(InputStream inputStream, Cfg cfg) {
        byte[] bytes = inputStream.readAllBytes();
        inputStream.close();

        List<MeasData> measurements = new ArrayList<>();
        int i = 0;

        while (i < bytes.length) {
            MeasData meas = new MeasData();
            i = i + 4;
            meas.setTime(((double) ParserUtil.bArrTo32UInt(bytes, i)) / 1_000_000);
            i = i + 4;

            List<AnalogMeasData> analogMeas = new ArrayList<>();
            for (AnalogCfg ac : cfg.getAnalogChannels()) {
                analogMeas.add(new AnalogMeasData(
                        ac.getChannelId(),
                        (ParserUtil.bArrTo16Int(bytes, i) * ac.getA() + ac.getB()) *
                                (ac.getValue() == AnalogCfg.Value.S ? ac.getPrimary() / ac.getSecondary() : 1)
                ));
                i = i + 2;
            }
            meas.setAnalogMeas(analogMeas);

            List<DigitalMeasData> digitalMeas = new ArrayList<>();
            for (int j = 0; j < Math.ceilDiv(cfg.getDigitalChannels().size(), 16); j++) {
                List<Boolean> arr = ParserUtil.bArrTo16Bit(bytes, i);
                for (int k = 0; k < 16; k++) {
                    if (cfg.getDigitalChannels().size() < j * 16 + k) break;
                    digitalMeas.add(new DigitalMeasData(cfg.getDigitalChannels().get(j * 16 + k).getChannelId(), arr.get(k)));
                }
                i = i + 2;
            }
            meas.setDigitalMeas(digitalMeas);

            measurements.add(meas);
        }


        return measurements;
    }

    @SneakyThrows
    private List<MeasData> asciiRead(InputStream inputStream, Cfg cfg) {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8.newDecoder()));

        String line;
        List<MeasData> measurements = new ArrayList<>();


        while ((line = bufferedReader.readLine()) != null) {
            MeasData meas = new MeasData();
            String[] l = line.split(",");
            int offset = 0;

            offset++;
            meas.setTime(Double.parseDouble(l[offset]) / 1_000_000);

            offset++;

            for (int i = 0; i < cfg.getAnalogChannels().size(); i++) {
                AnalogCfg ac = cfg.getAnalogChannels().get(i);
                meas.getAnalogMeas().add(new AnalogMeasData(ac.getChannelId(),
                        (Double.parseDouble(l[i + offset]) * ac.getA() + ac.getB()) *
                                (ac.getValue() == AnalogCfg.Value.S ? ac.getPrimary() / ac.getSecondary() : 1)));
            }
            offset = offset + cfg.getAnalogChannels().size();
            for (int i = 0; i < cfg.getDigitalChannels().size(); i++) {
                DigitalCfg ac = cfg.getDigitalChannels().get(i);
                meas.getDigitalMeas().add(new DigitalMeasData(ac.getChannelId(), Integer.parseInt(l[i + offset]) == 1));
            }
            measurements.add(meas);
        }

        bufferedReader.close();
        return measurements;
    }
}
