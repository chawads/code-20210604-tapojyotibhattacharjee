package com.btapo.interview.screening.bmi.controller;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import com.btapo.interview.screening.bmi.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/v1/bmi")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/upload")
    public BmiJobEntity upload(@RequestParam("file") MultipartFile file) {
        try {
            return applicationService.upload(file);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/job-status/{job-id}")
    public BmiJobEntity getJobStatus(@PathVariable("job-id") String jobId) {
        try {
            return applicationService.getJobStatus(jobId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/job-download-artifacts/{job-id}")
    public void downloadJobArtifacts(@PathVariable("job-id") String jobId) {
        try {
            File file = applicationService.getJobArtifacts(jobId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
