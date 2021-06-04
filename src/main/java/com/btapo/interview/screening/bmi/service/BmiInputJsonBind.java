package com.btapo.interview.screening.bmi.service;

import lombok.Data;

@Data
public class BmiInputJsonBind {
    private String Gender;
    private double HeightCm;
    private double WeightKg;
}