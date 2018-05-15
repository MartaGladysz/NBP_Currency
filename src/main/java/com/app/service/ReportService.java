package com.app.service;

import com.app.model.Report;
import com.app.model.ReportConfig;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;


public interface ReportService {
    String createMessage(Map<String, LinkedList<BigDecimal>> chosenCurrenciesMap, ReportConfig reportConfig);
    Optional<Report> getLastReport();
    ByteArrayInputStream currenciesReport(Report report);

}

