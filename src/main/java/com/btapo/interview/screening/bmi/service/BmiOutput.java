package com.btapo.interview.screening.bmi.service;

import lombok.Data;

@Data
public class BmiOutput {
    private String Gender;
    private Double HeightCm;
    private Double WeightKg;
    private Double BMI;
    private String Category;
    private String HealthRisk;

    public BmiOutput(String gender, double weightKg, double heightCm) {
        this.setGender(gender);
        this.setHeightCm(heightCm);
        this.setWeightKg(weightKg);
    }
}