package com.btapo.interview.screening.bmi.service;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ApplicationService {
    Double calculateBmi(double massInKg, double heightInCm);

    BmiJobEntity upload(MultipartFile file);

    BmiJobEntity getJobStatus(String jobId);

    File getJobArtifacts(String jobId);

    void updateJobStatus(String jobId, Long noOfRecordsProcessed, Long noOfRecordsWithError, Boolean completed,
                         Boolean successful);

    BmiConfig getBmiConfig(Double bmi);
}
