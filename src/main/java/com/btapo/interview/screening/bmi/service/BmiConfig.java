package com.btapo.interview.screening.bmi.service;

import lombok.Data;

@Data
public class BmiConfig {
    private double lowerThreshold, upperThreshold;
    private String category, healthRisk;
}
