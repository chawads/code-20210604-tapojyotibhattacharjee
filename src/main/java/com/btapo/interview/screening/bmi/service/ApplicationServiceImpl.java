package com.btapo.interview.screening.bmi.service;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import com.btapo.interview.screening.bmi.exception.RecordNotFoundException;
import com.btapo.interview.screening.bmi.repository.BmiJobRepository;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private static Collection<BmiConfig> bmiConfigs = new ArrayList<>();

    private final BmiJobRepository bmiJobRepository;

    public ApplicationServiceImpl(BmiJobRepository bmiJobRepository) throws IOException {
        this.bmiJobRepository = bmiJobRepository;
        loadBmiConfig();
    }

    private static synchronized void loadBmiConfig() throws IOException {
        if (bmiConfigs.size() > 0) {
            return;
        }
        InputStream in = ApplicationServiceImpl.class.getResourceAsStream("bmi-config.json");
        assert in != null;
        Gson gsonReader = new Gson();
        try (JsonReader reader = new JsonReader(new InputStreamReader(in))) {
            reader.beginArray();
            while (reader.hasNext()) {
                BmiConfig config = gsonReader.fromJson(reader, BmiConfig.class);
                bmiConfigs.add(config);
            }
            reader.endArray();
        }
    }

    @Override
    public BmiJobEntity upload(MultipartFile file) {
        String id = UUID.randomUUID().toString();
        BmiJobEntity entity = new BmiJobEntity(id);
        bmiJobRepository.save(entity);
        return entity;
    }

    @Override
    public BmiJobEntity getJobStatus(String jobId) {
        Optional<BmiJobEntity> jobEntity = bmiJobRepository.findById(jobId);
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
                                Boolean successful) {
        Optional<BmiJobEntity> jobEntity = bmiJobRepository.findById(jobId);
        if (!jobEntity.isPresent()) {
            throw new RecordNotFoundException("Job not found : " + jobId);
        }
        BmiJobEntity entity = jobEntity.get();
        entity.setCompleted(completed);
        entity.setNoOfRecordsProcessed(noOfRecordsProcessed);
        entity.setNoOfRecordsProcessedWithError(noOfRecordsWithError);
    }

    @Override
    public BmiConfig getBmiConfig(Double bmi) {
        for (BmiConfig config : bmiConfigs) {
            if (config.getLowerThreshold() >= bmi && config.getUpperThreshold() <= bmi) {
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
}
