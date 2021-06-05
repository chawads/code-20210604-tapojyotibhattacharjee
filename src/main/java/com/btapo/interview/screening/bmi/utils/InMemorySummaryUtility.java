package com.btapo.interview.screening.bmi.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySummaryUtility {

    private static final Map<String, InMemorySummaryEntity> summaryEntityMap = new ConcurrentHashMap<>();

    public static InMemorySummaryEntity add(InMemorySummaryEntity entity) {
        InMemorySummaryEntity summaryEntity = summaryEntityMap.computeIfAbsent(entity.getId(),
                k -> new InMemorySummaryEntity());
        summaryEntity.setId(entity.getId());
        summaryEntity.setName(entity.getName());
        summaryEntity.setDimensions(entity.getDimensions());
        for (Map.Entry<String, Double> incomingEntry : entity.getMeasures().entrySet()) {
            Double existingVal = summaryEntity.getMeasures().getOrDefault(incomingEntry.getKey(), 0d);
            summaryEntity.setMeasureValue(incomingEntry.getKey(), (existingVal + incomingEntry.getValue()));
        }
        return summaryEntity;
    }

    public static InMemorySummaryEntity get(String id) {
        return summaryEntityMap.get(id);
    }

    public static InMemorySummaryEntity delete(String id) {
        return summaryEntityMap.remove(id);
    }
}
