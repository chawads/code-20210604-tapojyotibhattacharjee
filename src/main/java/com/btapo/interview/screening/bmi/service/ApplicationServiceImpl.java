package com.btapo.interview.screening.bmi.service;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import com.btapo.interview.screening.bmi.exception.RecordNotFoundException;
import com.btapo.interview.screening.bmi.exception.UnexpectedException;
import com.btapo.interview.screening.bmi.repository.BmiJobRepository;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private static final Collection<BmiConfig> bmiConfigs = new ArrayList<>();
    private static ExecutorService executorService;
    private final BmiJobRepository bmiJobRepository;

    public ApplicationServiceImpl(@Value("${bmi.job.max-threads}") int maxThreads, BmiJobRepository bmiJobRepository) throws IOException {
        this.bmiJobRepository = bmiJobRepository;
        loadBmiConfig();
        initExecutorService(maxThreads);
    }

    private static synchronized void initExecutorService(int maxThreads) {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(maxThreads);
        }
    }

    private static synchronized void loadBmiConfig() throws IOException {
        if (bmiConfigs.size() > 0) {
            return;
        }
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("bmi-config.json")) {
            Gson gsonReader = new Gson();
            try (JsonReader reader = new JsonReader(new InputStreamReader(Objects.requireNonNull(in)))) {
                reader.beginArray();
                while (reader.hasNext()) {
                    BmiConfig config = gsonReader.fromJson(reader, BmiConfig.class);
                    bmiConfigs.add(config);
                }
                reader.endArray();
            }
        }
    }

    private static void saveFile(MultipartFile file, String fileName) {
        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();

                String formattedFileName = fileName;

                File serverFile = new File(formattedFileName);

                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();
            } else {
                throw new UnexpectedException("No file to upload");
            }
        } catch (IOException e) {
            throw new UnexpectedException("File IO error, can not upload");
        }
    }

    @Override
    public BmiJobEntity linkFile(String file) {
        String id = UUID.randomUUID().toString();
        return createJob(id, new File(file).getName(), new File(file), false);
    }

    @Override
    public BmiJobEntity uploadFile(MultipartFile file) throws IOException {
        String id = UUID.randomUUID().toString();
        File tmpFile = File.createTempFile(extractFileName(file, id), ".json");
        saveFile(file, tmpFile.getAbsolutePath());
        return createJob(id, file.getOriginalFilename(), tmpFile, true);
    }

    private String extractFileName(MultipartFile file, String defaultName) {
        return file.getOriginalFilename() != null ?
                file.getOriginalFilename().substring(0, file.getOriginalFilename().indexOf(".json"))
                : defaultName;
    }

    private BmiJobEntity createJob(String id, String inputFilename, File tmpFile, boolean deleteInputAfterProcess) {
        BmiJobEntity entity = new BmiJobEntity(id);
        String outDir = getDataOutDirectory(entity.getId());
        entity.setInputFileName(inputFilename);
        entity.setReportArtifactLocation(outDir + ".zip");
        bmiJobRepository.save(entity);
        startJob(entity, tmpFile, outDir, deleteInputAfterProcess);
        return entity;
    }

    private void startJob(BmiJobEntity entity, File tmpFile, String outDir, boolean deleteInputAfterProcess) {
        executorService.submit(new BmiCalculationJob(this, entity.getId(), tmpFile, outDir, deleteInputAfterProcess));
    }

    private String getDataOutDirectory(String id) {
        return System.getProperty("user.dir") + File.separator + "data" + File.separator + "out" + File.separator + id;
    }

    @Override
    public BmiJobEntity getJobStatus(String jobId) {
        Optional<BmiJobEntity> jobEntity = bmiJobRepository.findById(jobId);
        if (!jobEntity.isPresent()) {
            throw new RecordNotFoundException("Job not found : " + jobId);
        }
        return jobEntity.get();
    }

    @Override
    public File getJobArtifact(String jobId) {
        Optional<BmiJobEntity> jobEntity = bmiJobRepository.findById(jobId);
        if (!jobEntity.isPresent() || !jobEntity.get().getSuccessful()) {
            throw new RecordNotFoundException("Artifact not found : " + jobId);
        }
        return new File(jobEntity.get().getReportArtifactLocation());
    }

    @Override
    public void updateJobStatus(String jobId, Long noOfRecordsProcessed, Long noOfRecordsWithError, Boolean completed,
                                Boolean successful, Exception e) {
        Optional<BmiJobEntity> jobEntity = bmiJobRepository.findById(jobId);
        if (!jobEntity.isPresent()) {
            throw new RecordNotFoundException("Job not found : " + jobId);
        }
        BmiJobEntity entity = jobEntity.get();
        entity.setCompleted(completed);
        if (completed) {
            entity.setJobCompletedAt(new Date());
        }
        if (noOfRecordsProcessed != null) {
            entity.setNoOfRecordsProcessed(noOfRecordsProcessed);
        }
        if (noOfRecordsWithError != null) {
            entity.setNoOfRecordsProcessedWithError(noOfRecordsWithError);
        }
        if (entity.getJobStartedAt() == null) {
            entity.setJobStartedAt(new Date());
        }
        if (successful != null) {
            entity.setSuccessful(successful);
        }
        if (e != null) {
            entity.setErrorMessage(e.getMessage());
            entity.setErrorStackTrace(ExceptionUtils.getStackTrace(e));
        }
        bmiJobRepository.save(entity);
    }

    @Override
    public BmiConfig getBmiConfig(Double bmi) {
        for (BmiConfig config : bmiConfigs) {
            if (bmi >= config.getLowerThreshold() && bmi < config.getUpperThreshold()) {
                return config;
            }
        }
        return null;
    }

    @Override
    public Double calculateBmi(double massInKg, double heightInCm) {
        try {
            double heightInMetres = heightInCm / 100d;
            return massInKg / (heightInMetres * heightInMetres);
        } catch (Exception e) {
            return null;
        }
    }
}
