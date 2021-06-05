package com.btapo.interview.screening.bmi;

import com.btapo.interview.screening.bmi.utils.InMemorySummaryEntity;
import com.btapo.interview.screening.bmi.utils.InMemorySummaryUtility;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InMemorySummaryUtilTest {

    @Test
    @DisplayName("In memory summary creation should be successful")
    @Order(1)
    public void verifyInMemorySummaryCreationSuccess() {
        InMemorySummaryEntity entity = new InMemorySummaryEntity();
        entity.setName("test1");
        entity.setDimensionValue("k1", "1");
        entity.setDimensionValue("k2", "2");
        entity.setMeasureValue("m1", 1d);
        entity.setMeasureValue("m2", 2d);
        InMemorySummaryUtility.add(entity);
        if (InMemorySummaryUtility.get(entity.getId()) == null) throw new AssertionError();
    }

    @Test
    @DisplayName("In memory summary metric sum should be match")
    @Order(2)
    public void verifyInMemorySummaryAdditionSuccess_match() {
        InMemorySummaryEntity entity = new InMemorySummaryEntity();
        entity.setName("test1");
        entity.setDimensionValue("k1", "1");
        entity.setDimensionValue("k2", "2");
        entity.setMeasureValue("m1", 1d);
        entity.setMeasureValue("m2", 2d);
        InMemorySummaryUtility.add(entity);
        InMemorySummaryEntity summedEntity = InMemorySummaryUtility.get(entity.getId());
        assert summedEntity.getMeasures().get("m1") == 2;
        assert summedEntity.getMeasures().get("m2") == 4;
    }

    @Test
    @DisplayName("In memory summary metric sum should not match original value")
    @Order(3)
    public void verifyInMemorySummaryAdditionSuccess_mismatch() {
        InMemorySummaryEntity entity = new InMemorySummaryEntity();
        entity.setName("test1");
        entity.setDimensionValue("k1", "1");
        entity.setDimensionValue("k2", "2");
        entity.setMeasureValue("m1", 1d);
        entity.setMeasureValue("m2", 2d);
        InMemorySummaryUtility.add(entity);
        InMemorySummaryEntity summedEntity = InMemorySummaryUtility.get(entity.getId());
        assert summedEntity.getMeasures().get("m1") != 1;
        assert summedEntity.getMeasures().get("m2") != 2;
    }

    @Test
    @DisplayName("In memory summary different dimension insertion order should not impact result")
    @Order(4)
    public void verifyInMemorySummaryDifferentDimensionOrder_match() {
        InMemorySummaryEntity entity = new InMemorySummaryEntity();
        entity.setName("test1");
        entity.setDimensionValue("k2", "2");
        entity.setDimensionValue("k1", "1");
        InMemorySummaryEntity summedEntity = InMemorySummaryUtility.get(entity.getId());
        assert summedEntity.getMeasures().get("m1") == 3;
        assert summedEntity.getMeasures().get("m2") == 6;
    }

    @Test
    @DisplayName("In memory summary deletion should be successful")
    @Order(5)
    public void verifyInMemorySummaryDeletionSuccess() {
        InMemorySummaryEntity entity = new InMemorySummaryEntity();
        entity.setName("test1");
        entity.setDimensionValue("k1", "1");
        entity.setDimensionValue("k2", "2");
        InMemorySummaryUtility.delete(entity.getId());
        if (InMemorySummaryUtility.get(entity.getId()) != null) throw new AssertionError();
    }
}
