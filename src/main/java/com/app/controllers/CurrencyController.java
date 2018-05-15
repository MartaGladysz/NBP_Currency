package com.app.controllers;

import com.app.model.Currency;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CurrencyController {
    @GetMapping("/")
    public String CurrencySelectAll(Model model){
        Map<String,LinkedList<BigDecimal>> currenciesMap = Currency.allCurrencies("http://api.nbp.pl/api/exchangerates/tables/a/last/2/");

        Map<String, String> bestCurrenciesMap = currenciesMap
                .entrySet()
                .stream()
                .sorted((e1,e2) -> (e2.getValue().get(1).subtract(e2.getValue().get(0))).compareTo(e1.getValue().get(1).subtract(e1.getValue().get(0))))
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.valueOf(e.getValue().get(1).subtract(e.getValue().get(0)).setScale(4, RoundingMode.HALF_DOWN)),
                        (k,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", k));},
                        LinkedHashMap::new));

        model.addAttribute("currenciesMap", currenciesMap);
        model.addAttribute("bestCurrenciesMap", bestCurrenciesMap);
        return "index";
    }



}
