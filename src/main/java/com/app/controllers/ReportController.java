package com.app.controllers;

import com.app.model.Currency;
import com.app.model.Report;
import com.app.model.ReportConfig;
import com.app.repository.ReportRepository;
import com.app.service.ReportService;
import com.app.validators.ReportConfigValidator;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/report")
public class ReportController {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {

        binder.setValidator(new ReportConfigValidator());
    }

    private ReportService reportService;
    private ReportRepository reportRepository;

    public ReportController(ReportService reportService, ReportRepository reportRepository) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }

    @GetMapping("/reportAll")
    public String CurrencyReportAll(Model model){
        Map<String,LinkedList<BigDecimal>> currenciesMap = Currency.allCurrencies("http://api.nbp.pl/api/exchangerates/tables/a/last/2/");

        Map<String, String> currenciesSortMap = currenciesMap
                .entrySet()
                .stream()
                .sorted((e1,e2) -> (e2.getValue().get(1).subtract(e2.getValue().get(0))).compareTo(e1.getValue().get(1).subtract(e1.getValue().get(0))))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.valueOf(e.getValue().get(1).subtract(e.getValue().get(0)).setScale(4, RoundingMode.HALF_DOWN)),
                        (k,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", k));},
                        LinkedHashMap::new));

        model.addAttribute("currenciesSortMap", currenciesSortMap);

        return "report/reportAll";
    }

    @GetMapping("/choice")
    public String choiceGet(Model model){
        Map<String, LinkedList<BigDecimal>> currenciesMap = Currency.allCurrencies("http://api.nbp.pl/api/exchangerates/tables/a/last/2/");
        List<String> currenciesList = new ArrayList<>(currenciesMap.keySet());

        model.addAttribute("reportConfig", new ReportConfig(new ArrayList<>(), 0));
        model.addAttribute("currenciesList", currenciesList);
        model.addAttribute("errors", new HashMap<>());
        return "report/choice";
    }

    @PostMapping("/choice")
    public String choicePost(Model model, @ModelAttribute @Valid ReportConfig reportConfig, BindingResult result){
        Map<String, LinkedList<BigDecimal>> currenciesMap = Currency.allCurrencies("http://api.nbp.pl/api/exchangerates/tables/a/last/2/");
        List<String> currenciesList = new ArrayList<>(currenciesMap.keySet());
        Map<String, String> errors = new HashMap<>();

        if(result.hasErrors()){
            List<FieldError> errorsList = result.getFieldErrors();
            for(FieldError e: errorsList){
                errors.put(e.getField(), e.getCode());
            }

            model.addAttribute("reportConfig", reportConfig);
            model.addAttribute("currenciesList", currenciesList);
            model.addAttribute("errors", errors);

            return "report/choice";
        }

        Map<String, LinkedList<BigDecimal>> currenciesMap1 = Currency.allCurrencies("http://api.nbp.pl/api/exchangerates/tables/a/last/"
                +reportConfig.getNumberOfQuotations()+"/");

        Map<String, LinkedList<BigDecimal>> chosenCurrenciesMap = currenciesMap1.entrySet()
                .stream()
                .filter(e -> reportConfig.getCurrenciesList().contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

        List<List<BigDecimal>> statisticsList = new LinkedList<>();
        List<BigDecimal> list;
        for(LinkedList<BigDecimal> quotationsList: chosenCurrenciesMap.values()){
            list = new LinkedList<>();
            list.add(quotationsList.stream().min(BigDecimal::compareTo).get());
            list.add(quotationsList.stream().max(BigDecimal::compareTo).get());
            list.add(new BigDecimal(quotationsList.stream().mapToDouble(BigDecimal::doubleValue).average().getAsDouble()).setScale(4,RoundingMode.HALF_DOWN));
            statisticsList.add(list);
        }

        String message = reportService.createMessage(chosenCurrenciesMap, reportConfig);
        Report report = Report.builder().date(LocalDate.now()).message(message).build();
        reportRepository.save(report);

        model.addAttribute("currenciesMapChosen", chosenCurrenciesMap);
        model.addAttribute("statisticsList", statisticsList);
        return "/report/reportChosen";
    }

    @GetMapping(value = "/reportPDF", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> currenciesReport(){
        Report report = reportService.getLastReport().orElseThrow(NullPointerException::new);

        ByteArrayInputStream bis = reportService.currenciesReport(report);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=currenciesReport.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
