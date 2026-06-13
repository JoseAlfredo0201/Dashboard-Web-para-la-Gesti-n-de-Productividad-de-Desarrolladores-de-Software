package com.exampleback.demo.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.exampleback.demo.dto.MetricDefinitionDTO;

public enum MetricType {

    COMMITS("commits", "Commits") {
        @Override
        public int extractValue(DeveloperMetric metric) {
            return safeValue(metric.getCommits());
        }
    },
    BUGS("bugs", "Incidencias resueltas") {
        @Override
        public int extractValue(DeveloperMetric metric) {
            return safeValue(metric.getBugsFixed());
        }
    },
    TASKS("tasks", "Tareas completadas") {
        @Override
        public int extractValue(DeveloperMetric metric) {
            return safeValue(metric.getTasksCompleted());
        }
    },
    STORY_POINTS("storyPoints", "Story points") {
        @Override
        public int extractValue(DeveloperMetric metric) {
            return safeValue(metric.getStoryPoints());
        }
    };

    private final String key;
    private final String label;

    MetricType(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public abstract int extractValue(DeveloperMetric metric);

    public static Optional<MetricType> fromKey(String key) {
        return Arrays.stream(values())
                .filter(metricType -> metricType.key.equalsIgnoreCase(key))
                .findFirst();
    }

    public static List<MetricDefinitionDTO> definitions() {
        return Arrays.stream(values())
                .map(metricType -> new MetricDefinitionDTO(
                        metricType.getKey(),
                        metricType.getLabel()))
                .toList();
    }

    private static int safeValue(Integer value) {
        return value == null ? 0 : value;
    }
}