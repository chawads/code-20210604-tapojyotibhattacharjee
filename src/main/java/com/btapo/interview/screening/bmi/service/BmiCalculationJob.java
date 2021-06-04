package com.btapo.interview.screening.bmi.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;

@Slf4j
public class BmiCalculationJob implements Runnable {

    private final File input;
    private final String outputDir;
    private final ApplicationService service;
    private final String jobId;
    private final boolean deleteInputAfterProcess;

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
//                Files.move(Paths.get(summaryFile.getAbsolutePath()), Paths.get(outputDir + File.separator + summaryFile.getName()),
//                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                service.compressZipFile(outputDir, outputDir + ".zip");
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
                    service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, false, true, null);
                }
            }
            writer.endArray();
            reader.endArray();
            service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, true, true, null);
            return true;
        } catch (Exception e) {
            log.error("File processing failed : {}", inPath);
            service.updateJobStatus(jobId, noOfRecordsProcessed, noOfRecordsWithError, true, false, e);
            return false;
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
