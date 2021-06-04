package com.btapo.interview.screening.bmi.controller;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import com.btapo.interview.screening.bmi.exception.RecordNotFoundException;
import com.btapo.interview.screening.bmi.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;

@Slf4j
@RestController
@RequestMapping("/v1/bmi")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/job-submit-by-file-link")
    public BmiJobEntity jobSubmitByFileLink(@RequestParam("absoluteFilePath") String absoluteFilePath) {
        try {
            return applicationService.linkFile(absoluteFilePath);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/job-submit-by-file-upload")
    public BmiJobEntity jobSubmitByFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            return applicationService.uploadFile(file);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/job-status/{job-id}")
    public BmiJobEntity getJobStatus(@PathVariable("job-id") String jobId) {
        try {
            return applicationService.getJobStatus(jobId);
        } catch (RecordNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/job-download-artifacts/{job-id}")
    public ResponseEntity<Resource> downloadJobArtifact(@PathVariable("job-id") String jobId) {
        try {
            File file = applicationService.getJobArtifact(jobId);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Disposition", "attachment; filename=" + file.getName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (RecordNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
