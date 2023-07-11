package ru.mpei.parser.service;

import ru.mpei.parser.model.dto.FaultData;

public interface AnaliseService {
    FaultData analiseMeas(long id, String phA, String phB, String phC);

    FaultData analiseMeas(long id, String phA, String phB, String phC, double stock);
}
