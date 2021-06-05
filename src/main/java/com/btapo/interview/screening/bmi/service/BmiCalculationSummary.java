package com.btapo.interview.screening.bmi.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Data
public class BmiCalculationSummary {
    private Collection<Map<String, Object>> categoryWiseSummary = new ArrayList<>();
    private Collection<Map<String, Object>> healthRiskWiseSummary = new ArrayList<>();
    private Collection<Map<String, Object>> categoryHealthRiskWiseSummary = new ArrayList<>();
}
