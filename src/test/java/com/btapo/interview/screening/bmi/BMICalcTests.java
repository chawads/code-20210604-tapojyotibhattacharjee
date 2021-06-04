package com.btapo.interview.screening.bmi;

import com.btapo.interview.screening.bmi.service.ApplicationService;
import com.btapo.interview.screening.bmi.service.BmiConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.text.DecimalFormat;

@SpringBootTest
@ActiveProfiles("test")
public class BMICalcTests {

    @Autowired
    private ApplicationService applicationService;

    @Test
    @DisplayName("BMI should be 20.1")
    public void verifyBMICalculationMethod_match_1() {
        DecimalFormat df = new DecimalFormat("0.0");
        assert df.format(applicationService.calculateBmi(65, 180)).equals(df.format(20.1));
        assert !df.format(applicationService.calculateBmi(65, 180)).equals(df.format(31.1));
    }

    @Test
    @DisplayName("BMI should be 26.4")
    public void verifyBMICalculationMethod_match_2() {
        DecimalFormat df = new DecimalFormat("0.0");
        assert df.format(applicationService.calculateBmi(80, 174)).equals(df.format(26.4));
    }

    @Test
    @DisplayName("BMI should not match")
    public void verifyBMICalculationMethod_shouldNotMatch_1() {
        DecimalFormat df = new DecimalFormat("0.0");
        assert !df.format(applicationService.calculateBmi(65, 180)).equals(df.format(31.1));
    }

    @Test
    @DisplayName("BMI category should be 'Underweight' and health risk should be 'Malnutrition risk'")
    public void verifyBMIConfigMethod_Underweight_MalnutritionRisk_match() {
        {
            Double bmi = applicationService.calculateBmi(55, 180);
            BmiConfig config = applicationService.getBmiConfig(bmi);
            assert config.getCategory().equals("Underweight");
            assert config.getHealthRisk().equals("Malnutrition risk");
        }
    }

    @Test
    @DisplayName("BMI category should be 'Normal weight' and health risk should be 'Low risk'")
    public void verifyBMIConfigMethod_NormalWeight_LowRisk() {
        {
            Double bmi = applicationService.calculateBmi(75, 180);
            BmiConfig config = applicationService.getBmiConfig(bmi);
            assert config.getCategory().equals("Normal weight");
            assert config.getHealthRisk().equals("Low risk");
        }
    }

    @Test
    @DisplayName("Boundary condition check BMI category should be 'Overweight' and health risk should be 'Enhanced risk'")
    public void verifyBMIConfigMethod_BoundaryCondition_Overweight_EnhancedRisk() {
        {
            Double bmi = applicationService.calculateBmi(97, 180);
            BmiConfig config = applicationService.getBmiConfig(bmi);
            assert config.getCategory().equals("Overweight");
            assert config.getHealthRisk().equals("Enhanced risk");
        }
    }

    @Test
    @DisplayName("Illegal value check, config should be null")
    public void verifyBMIConfigMethod_IllegalValueCheck() {
        {
            Double bmi = applicationService.calculateBmi(0, 0);
            BmiConfig config = applicationService.getBmiConfig(bmi);
            assert config == null;
        }
    }
}