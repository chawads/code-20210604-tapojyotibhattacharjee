package com.btapo.interview.screening.bmi.service;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface ApplicationService {
    Double calculateBmi(double massInKg, double heightInCm);

    BmiJobEntity linkFile(String absoluteFilePath);

    BmiJobEntity getJobStatus(String jobId);

    File getJobArtifact(String jobId);

    void updateJobStatus(String jobId, Long noOfRecordsProcessed, Long noOfRecordsWithError, Boolean completed,
                         Boolean successful, Exception e);

    BmiConfig getBmiConfig(Double bmi);

    BmiJobEntity uploadFile(MultipartFile file) throws IOException;
}
