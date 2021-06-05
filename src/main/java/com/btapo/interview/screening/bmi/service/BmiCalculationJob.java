package com.btapo.interview.screening.bmi.service;

import com.btapo.interview.screening.bmi.utils.InMemorySummaryEntity;
import com.btapo.interview.screening.bmi.utils.InMemorySummaryUtility;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.btapo.interview.screening.bmi.utils.CompressionUtility.compressZipFile;

@Slf4j
public class BmiCalculationJob implements Runnable {

    private final File input;
    private final String outputDir;
    private final ApplicationService service;
    private final String jobId;
    private final boolean deleteInputAfterProcess;
    private Set<String> summaryIds = new HashSet<>();

    public BmiCalculationJob(ApplicationService service, String jobId, File input, String outputDir,
                             boolean deleteInputAfterProcess) {
        this.service = service;
        this.jobId = jobId;
        this.input = input;
        this.outputDir = outputDir;
        this.deleteInputAfterProcess = deleteInputAfterProcess;
    }

    @Override
    public void run() {
        try {
            service.updateJobStatus(jobId, null, null, false, null, null);
            File outFile = File.createTempFile("out-"+System.currentTimeMillis(), ".json");
            File summaryFile = File.createTempFile("summary-" + System.currentTimeMillis(), ".json");
            Files.createDirectories(Paths.get(outputDir));
            if (process(input.getAbsolutePath(), outFile.getAbsolutePath(), summaryFile.getAbsolutePath())) {
                Files.move(Paths.get(outFile.getAbsolutePath()), Paths.get(outputDir + File.separator + outFile.getName()),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                Files.move(Paths.get(summaryFile.getAbsolutePath()), Paths.get(outputDir + File.separator + summaryFile.getName()),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                compressZipFile(outputDir, outputDir + ".zip");
                FileUtils.deleteDirectory(new File(outputDir));
            }
        } catch (IOException e) {
            log.error("Failed to process for job : {}", jobId, e);
            service.updateJobStatus(jobId, null, null, true, false, e);
        } finally {
            if (deleteInputAfterProcess) {
                if (input.delete()) {
                    log.info("Successfully deleted input file : {}", input);
                } else {
                    log.info("Failed to delete input file : {}", input);
                }
            }
        }
    }

    private boolean process(String inPath, String outPath, String summaryPath) throws IOException {
        Long noOfRecordsProcessed = 0L;
        Long noOfRecordsWithError = 0L;
        int updateStatusEveryXRecords = 1_000;
        DecimalFormat format = new DecimalFormat("0.00");
        try (
                InputStream inputStream = Files.newInputStream(Paths.get(inPath));
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                JsonWriter writer = new JsonWriter(new FileWriter(outPath))
        ) {
            reader.beginArray();
            writer.setSerializeNulls(false);
            writer.setLenient(true);
            writer.beginArray();
            Gson gsonReader = new Gson();
            while (reader.hasNext()) {
                BmiInputJsonBind bmiInput = gsonReader.fromJson(reader, BmiInputJsonBind.class);
                Double bmi = service.calculateBmi(bmiInput.getWeightKg(), bmiInput.getHeightCm());
                noOfRecordsProcessed++;
                BmiOutput bmiOutput = new BmiOutput(bmiInput.getGender(), bmiInput.getWeightKg(),
                        bmiInput.getHeightCm());
                if (bmi == null || bmi < 1 || bmi >= 9999) {
                    noOfRecordsWithError++;
                } else {
                    BmiConfig bmiConfig = service.getBmiConfig(bmi);
                    bmiOutput.setBMI(Double.valueOf(format.format(bmi)));
                    bmiOutput.setCategory(bmiConfig.getCategory());
                    bmiOutput.setHealthRisk(bmiConfig.getHealthRisk());
                }
                writer.beginObject();
                writeOutput(bmiOutput, writer);
                writer.endObject();

                addSummary(bmiOutput);

                if (noOfRecordsProcessed % updateStatusEveryXRecords == 0) {
                    service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, false, true, null);
                }
            }
            writer.endArray();
            reader.endArray();
            writeSummary(summaryPath);
            service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, true, true, null);
            return true;
        } catch (Exception e) {
            log.error("File processing failed : {}", inPath);
            service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, true, false, e);
            return false;
        }
    }

    private void writeSummary(String summaryPath) throws IOException {
        BmiCalculationSummary calculationSummary = new BmiCalculationSummary();
        for (String summaryId : summaryIds) {
            InMemorySummaryEntity entity = InMemorySummaryUtility.get(summaryId);
            if (getCategorySummaryName().equals(entity.getName())) {
                Map<String, Object> map = new HashMap<>();
                map.putAll(entity.getDimensions());
                map.putAll(entity.getMeasures());
                calculationSummary.getCategoryWiseSummary().add(map);
            } else if (getHealthRiskSummaryName().equals(entity.getName())) {
                Map<String, Object> map = new HashMap<>();
                map.putAll(entity.getDimensions());
                map.putAll(entity.getMeasures());
                calculationSummary.getHealthRiskWiseSummary().add(map);
            } else if (getCategoryHealthRiskSummaryName().equals(entity.getName())) {
                Map<String, Object> map = new HashMap<>();
                map.putAll(entity.getDimensions());
                map.putAll(entity.getMeasures());
                calculationSummary.getCategoryHealthRiskWiseSummary().add(map);
            }
        }
        Gson gson = new Gson();
        Files.write(Paths.get(summaryPath), gson.toJson(calculationSummary).getBytes(StandardCharsets.UTF_8));
    }

    private void addSummary(BmiOutput bmiOutput) {
        {
            InMemorySummaryEntity entity = new InMemorySummaryEntity();
            entity.setName(getCategorySummaryName());
            entity.setDimensionValue("category", getUnknownIfNull(bmiOutput.getCategory()));
            entity.setMeasureValue("count", 1d);
            InMemorySummaryUtility.add(entity);
            summaryIds.add(entity.getId());
        }
        {
            InMemorySummaryEntity entity = new InMemorySummaryEntity();
            entity.setName(getHealthRiskSummaryName());
            entity.setDimensionValue("healthRisk", getUnknownIfNull(bmiOutput.getHealthRisk()));
            entity.setMeasureValue("count", 1d);
            InMemorySummaryUtility.add(entity);
            summaryIds.add(entity.getId());
        }
        {
            InMemorySummaryEntity entity = new InMemorySummaryEntity();
            entity.setName(getCategoryHealthRiskSummaryName());
            entity.setDimensionValue("category", getUnknownIfNull(bmiOutput.getCategory()));
            entity.setDimensionValue("healthRisk", getUnknownIfNull(bmiOutput.getHealthRisk()));
            entity.setMeasureValue("count", 1d);
            InMemorySummaryUtility.add(entity);
            summaryIds.add(entity.getId());
        }
    }

    private String getCategorySummaryName() {
        return jobId + "-" + "CategoryWiseSummary";
    }

    private String getHealthRiskSummaryName() {
        return jobId + "-" + "HealthRiskWiseSummary";
    }

    private String getCategoryHealthRiskSummaryName() {
        return jobId + "-" + "CategoryHealthRiskWiseSummary";
    }

    private String getUnknownIfNull(String v) {
        return v == null || v.isEmpty() ? "unknown" : v;
    }

    private void writeOutput(BmiOutput bmiOutput, JsonWriter writer) throws IOException {
        writer.name("Gender").value(bmiOutput.getGender());
        writer.name("HeightCm").value(bmiOutput.getHeightCm());
        writer.name("WeightKg").value(bmiOutput.getWeightKg());
        writer.name("BMI").value(bmiOutput.getBMI());
        writer.name("Category").value(bmiOutput.getCategory());
        writer.name("HealthRisk").value(bmiOutput.getHealthRisk());
    }
}
