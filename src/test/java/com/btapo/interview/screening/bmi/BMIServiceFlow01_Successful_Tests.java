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
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.context.junit4.SpringRunner;
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
public class BMIServiceFlow01_Successful_Tests {

    private final String inFilePath;
    private final String expectedOutFilePath;
    private final String expectedSummaryFilePath;
    private static AtomicReference<Path> decompressedDir = new AtomicReference<>();
    private static BmiJobEntity entity;

    @Autowired
    private ApplicationService applicationService;

    public BMIServiceFlow01_Successful_Tests() {
        inFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "in" + File.separator + "01-bmi-sample-original.json";
        expectedOutFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "in" + File.separator + "01-bmi-sample-original-expected-out.json";
        expectedSummaryFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "in" + File.separator + "01-bmi-sample-original-expected-summary.json";
    }

    @Test
    @BeforeTestClass
    @DisplayName("BMI test for init for original sample prefixed '01'")
    @Order(1)
    public void sample_01_init() {
        entity = applicationService.linkFile(inFilePath);
        assert entity != null;
        // keep checking till processing completed
        BmiJobEntity finalEntity = entity;
        await().until(() -> applicationService.getJobStatus(finalEntity.getId()).getCompleted());

        entity = applicationService.getJobStatus(entity.getId());
        decompressedDir.set(Paths.get(entity.getReportArtifactLocation().substring(0, entity.getReportArtifactLocation().indexOf(".zip"))));
    }

    @Test
    @DisplayName("BMI test for cleanup for original sample prefixed '01'")
    @AfterTestClass
    public void sample_01_cleanup() throws IOException {
        // cleanup
        FileUtils.deleteDirectory(decompressedDir.get().toFile());
        FileUtils.delete(new File(decompressedDir.get() + ".zip"));
    }

    @Test
    @DisplayName("BMI test for processing successful for original sample prefixed '01'")
    @Order(2)
    public void sample_01_processing_success() {
        // make sure the job is successful
        Assert.isTrue(entity.getSuccessful(), "Processing failed");
    }

    @Test
    @DisplayName("BMI test for file count generated for original sample prefixed '01'")
    @Order(3)
    public void sample_01_numberOfFilesGenerated_success() throws IOException {
        // get the artifacts generated and unzip
        await().until(() -> {
            try {
                decompressedDir.set(Paths.get(entity.getReportArtifactLocation().substring(0, entity.getReportArtifactLocation().indexOf(".zip"))));
                CompressionUtility.decompressZip(decompressedDir.get().toString(), entity.getReportArtifactLocation());
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        assert Files.list(decompressedDir.get()).count() == 2;
        assert Files.list(decompressedDir.get())
                .map(path -> path.getFileName().toFile().getName())
                .filter(f -> f.startsWith("out")).count() == 1;
        assert Files.list(decompressedDir.get())
                .map(path -> path.getFileName().toFile().getName())
                .filter(f -> f.startsWith("summary")).count() == 1;
    }

    @Test
    @DisplayName("BMI test for 'out' file validation for original sample prefixed '01'")
    @Order(4)
    public void sample_01_outFileValidation_success() throws IOException {
        Path generatedOutFilePath = Files.list(decompressedDir.get())
                .filter(path -> path.getFileName().toFile().getName().startsWith("out"))
                .collect(Collectors.toList()).get(0);

        Map<String, BmiOutput> generatedMap = new HashMap<>();
        Map<String, BmiOutput> expectedMap = new HashMap<>();
        DecimalFormat df = new DecimalFormat("0.00");
        try (
                InputStream inputStreamExpected = Files.newInputStream(Paths.get(expectedOutFilePath));
                JsonReader readerExpected = new JsonReader(new InputStreamReader(inputStreamExpected));
                InputStream inputStreamGenerated = Files.newInputStream(generatedOutFilePath);
                JsonReader readerGenerated = new JsonReader(new InputStreamReader(inputStreamGenerated))
        ) {
            {
                readerExpected.beginArray();
                Gson gsonReaderExpected = new Gson();
                while (readerExpected.hasNext()) {
                    BmiOutput expectedOutput = gsonReaderExpected.fromJson(readerExpected, BmiOutput.class);
                    String key = getKey(expectedOutput, df);
                    expectedMap.put(key, expectedOutput);
                }
                readerExpected.endArray();
            }
            {
                readerGenerated.beginArray();
                Gson gsonReaderGenerated = new Gson();
                while (readerGenerated.hasNext()) {
                    BmiOutput expectedOutput = gsonReaderGenerated.fromJson(readerGenerated, BmiOutput.class);
                    String key = getKey(expectedOutput, df);
                    generatedMap.put(key, expectedOutput);
                }
                readerGenerated.endArray();
            }
            for (Map.Entry<String, BmiOutput> entry : generatedMap.entrySet()) {
                assert entry.getValue().getGender().equals(expectedMap.get(entry.getKey()).getGender());
                assert entry.getValue().getWeightKg().equals(expectedMap.get(entry.getKey()).getWeightKg());
                assert entry.getValue().getHeightCm().equals(expectedMap.get(entry.getKey()).getHeightCm());
                assert entry.getValue().getBMI().equals(expectedMap.get(entry.getKey()).getBMI());
                assert entry.getValue().getCategory().equals(expectedMap.get(entry.getKey()).getCategory());
                assert entry.getValue().getHealthRisk().equals(expectedMap.get(entry.getKey()).getHealthRisk());
            }
        }
    }

    private String getKey(BmiOutput expectedOutput, DecimalFormat df) {
        return expectedOutput.getGender() + "-" + df.format(expectedOutput.getWeightKg())
                + "-" + df.format(expectedOutput.getHeightCm());
    }

    @Test
    @DisplayName("BMI test for 'summary' file validation for original sample prefixed '01'")
    @Order(5)
    public void sample_01_summaryFileValidation_success() throws IOException {
        Path generatedSummaryFilePath = Files.list(decompressedDir.get())
                .filter(path -> path.getFileName().toFile().getName().startsWith("summary"))
                .collect(Collectors.toList()).get(0);

        Map<String, Double> expectedMap = new HashMap<>();
        Map<String, Double> generatedMap = new HashMap<>();
        {
            Gson gsonReaderExpected = new Gson();
            BmiCalculationSummary summary = gsonReaderExpected
                    .fromJson(new String(Files.readAllBytes(Paths.get(expectedSummaryFilePath)),
                            StandardCharsets.UTF_8), BmiCalculationSummary.class);
            for (Map<String, Object> map : summary.getCategoryWiseSummary()) {
                String category = (String) map.get("category");
                Double count = (Double) map.get("count");
                expectedMap.put(getSummaryKey("categoryWiseSummary", "category", category), count);
            }
            for (Map<String, Object> map : summary.getHealthRiskWiseSummary()) {
                String healthRisk = (String) map.get("healthRisk");
                Double count = (Double) map.get("count");
                expectedMap.put(getSummaryKey("healthRiskWiseSummary", "healthRisk", healthRisk), count);
            }
            for (Map<String, Object> map : summary.getCategoryHealthRiskWiseSummary()) {
                String category = (String) map.get("category");
                String healthRisk = (String) map.get("healthRisk");
                Double count = (Double) map.get("count");
                expectedMap.put(getSummaryKey("healthRiskWiseSummary", "category", category,
                        "healthRisk", healthRisk), count);
            }
        }
        {
            Gson gsonReaderGenerated = new Gson();
            BmiCalculationSummary summary = gsonReaderGenerated
                    .fromJson(new String(Files.readAllBytes(generatedSummaryFilePath),
                            StandardCharsets.UTF_8), BmiCalculationSummary.class);
            for (Map<String, Object> map : summary.getCategoryWiseSummary()) {
                String category = (String) map.get("category");
                Double count = (Double) map.get("count");
                generatedMap.put(getSummaryKey("categoryWiseSummary", "category", category), count);
            }
            for (Map<String, Object> map : summary.getHealthRiskWiseSummary()) {
                String healthRisk = (String) map.get("healthRisk");
                Double count = (Double) map.get("count");
                generatedMap.put(getSummaryKey("healthRiskWiseSummary", "healthRisk", healthRisk), count);
            }
            for (Map<String, Object> map : summary.getCategoryHealthRiskWiseSummary()) {
                String category = (String) map.get("category");
                String healthRisk = (String) map.get("healthRisk");
                Double count = (Double) map.get("count");
                generatedMap.put(getSummaryKey("healthRiskWiseSummary", "category", category,
                        "healthRisk", healthRisk), count);
            }
        }
        assert generatedMap.size() == expectedMap.size();
        for (Map.Entry<String, Double> entry : generatedMap.entrySet()) {
            assert entry.getValue().equals(expectedMap.get(entry.getKey()));
        }
    }

    private String getSummaryKey(String... keys) {
        return StringUtils.join(keys, '-');
    }
}