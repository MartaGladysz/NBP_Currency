package com.app.validators;

import com.app.model.ReportConfig;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ReportConfigValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(ReportConfig.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ReportConfig r = (ReportConfig) o;
        if(r.getCurrenciesList() == null || (r.getCurrenciesList().size() < 1)){
            errors.rejectValue("currenciesList", "Proszę wybrać co najmniej jedną z walut!");
        }
    }
}
