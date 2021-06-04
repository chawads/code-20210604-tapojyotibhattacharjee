package com.btapo.interview.screening.bmi.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

@Slf4j
public class BmiJob implements Runnable {

    private final File input;
    private final String outputReportZipName;
    private final ApplicationService service;
    private final String jobId;

    public BmiJob(ApplicationService service, String jobId, File input, String outputReportZipName) {
        this.service = service;
        this.jobId = jobId;
        this.input = input;
        this.outputReportZipName = outputReportZipName;
    }

    @Override
    public void run() {
        try {
            service.updateJobStatus(jobId, null, null, false, null);
            File outFile = File.createTempFile(System.currentTimeMillis() + "-out-" + jobId, ".json");
            File summaryFile = File.createTempFile(System.currentTimeMillis() + "-summary-" + jobId, ".json");
            process(input.getAbsolutePath(), outFile.getAbsolutePath(), summaryFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to process for job : {}", jobId);
            service.updateJobStatus(jobId, null, null, true, false);
        }
    }

    private void process(String inPath, String outPath, String summaryPath) throws IOException {
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
                if (bmi == null) {
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

                if (noOfRecordsProcessed % updateStatusEveryXRecords == 0) {
                    service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, false, true);
                }
            }
            writer.endArray();
            reader.endArray();
            service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, true, true);
        } catch (Exception e) {
            service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, true, false);
        }
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
