package com.btapo.interview.screening.bmi.repository;

import com.btapo.interview.screening.bmi.entity.BmiJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BmiJobRepository extends JpaRepository<BmiJobEntity, String> {
}
