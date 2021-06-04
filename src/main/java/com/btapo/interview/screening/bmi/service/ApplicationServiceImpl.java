package com.btapo.interview.screening.bmi.service;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import com.btapo.interview.screening.bmi.exception.RecordNotFoundException;
import com.btapo.interview.screening.bmi.exception.UnexpectedException;
import com.btapo.interview.screening.bmi.repository.BmiJobRepository;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private static final Collection<BmiConfig> bmiConfigs = new ArrayList<>();
    private static ExecutorService executorService;
    private final BmiJobRepository bmiJobRepository;
    private final int maxThreads;

    public ApplicationServiceImpl(@Value("${bmi.job.max-threads}") int maxThreads, BmiJobRepository bmiJobRepository) throws IOException {
        this.bmiJobRepository = bmiJobRepository;
        this.maxThreads = maxThreads;
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

    private static void compressDirectoryToZipFile(String rootDir, String sourceDir, ZipOutputStream out) throws IOException {
        for (File file : Objects.requireNonNull(new File(sourceDir).listFiles())) {
            if (file.isDirectory()) {
                compressDirectoryToZipFile(rootDir, sourceDir + File.separator + file.getName(), out);
            } else {
                ZipEntry entry = new ZipEntry(sourceDir.replace(rootDir, "") + file.getName());
                out.putNextEntry(entry);

                FileInputStream in = new FileInputStream(sourceDir + File.separator + file.getName());
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
            }
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
        entity.setInputFileName(inputFilename);
        bmiJobRepository.save(entity);
        startJob(entity, tmpFile, deleteInputAfterProcess);
        return entity;
    }

    private void startJob(BmiJobEntity entity, File tmpFile, boolean deleteInputAfterProcess) {
        String outDir = getDataOutDirectory(entity.getId());
        executorService.submit(new BmiJob(this, entity.getId(), tmpFile, outDir, deleteInputAfterProcess));
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
    public File getJobArtifacts(String jobId) {
        Optional<BmiJobEntity> jobEntity = bmiJobRepository.findById(jobId);
        if (!jobEntity.isPresent()) {
            throw new RecordNotFoundException("Job not found : " + jobId);
        }
        return new File("");
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
            if (bmi >= config.getLowerThreshold() && bmi <= config.getUpperThreshold()) {
                return config;
            }
        }
        return null;
    }

    @Override
    public Double calculateBmi(double massInKg, double heightInCm) {
        try {
            double heightInMetres = heightInCm / 100d;
            return massInKg / heightInMetres;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void compressZipFile(String sourceDir, String outputFile) throws IOException {
        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(outputFile));
        compressDirectoryToZipFile(sourceDir, sourceDir, zipFile);
        IOUtils.closeQuietly(zipFile);
    }
}
