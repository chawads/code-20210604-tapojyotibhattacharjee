package com.btapo.interview.screening.bmi;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import com.btapo.interview.screening.bmi.service.ApplicationService;
import com.btapo.interview.screening.bmi.service.BmiCalculationSummary;
import com.btapo.interview.screening.bmi.service.BmiOutput;
import com.btapo.interview.screening.bmi.utils.CompressionUtility;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BMIServiceFlowTests03_SyntaxFailure {

    private final String inFilePath;
    private static BmiJobEntity entity;

    @Autowired
    private ApplicationService applicationService;

    public BMIServiceFlowTests03_SyntaxFailure() {
        inFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "in" + File.separator + "03-bmi-sample-invalid-syntax-file.json";
    }

    @Test
    @BeforeTestClass
    @DisplayName("BMI test for init for sample prefixed '03'")
    @Order(1)
    public void sample_03_init() {
        entity = applicationService.linkFile(inFilePath);
        assert entity != null;
        // keep checking till processing completed
        BmiJobEntity finalEntity = entity;
        await().until(() -> applicationService.getJobStatus(finalEntity.getId()).getCompleted());

        entity = applicationService.getJobStatus(entity.getId());
    }

    @Test
    @DisplayName("BMI test for processing failure for sample prefixed '03' with syntax error")
    @Order(2)
    public void sample_03_processing_failure_expected() {
        // make sure the job is failed
        Assert.isTrue(!entity.getSuccessful(), "Processing should have failed");
    }
}