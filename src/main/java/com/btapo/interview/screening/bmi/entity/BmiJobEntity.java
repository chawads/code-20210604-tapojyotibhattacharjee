package com.btapo.interview.screening.bmi.entity;

import com.sun.istack.internal.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "bmi_job")
public class BmiJobEntity {
    @NotNull
    @Id
    private String id;
    private Boolean completed;
    private Boolean successful;
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Date jobSubmittedAt;
    private Date jobStartedAt;
    private Date jobCompletedAt;
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date jobLastUpdatedAt;
    private long noOfRecordsProcessed;
    private long noOfRecordsProcessedWithError;
    private String reportSummary;

    public BmiJobEntity(String jobId) {
        this.id = jobId;
    }
}
