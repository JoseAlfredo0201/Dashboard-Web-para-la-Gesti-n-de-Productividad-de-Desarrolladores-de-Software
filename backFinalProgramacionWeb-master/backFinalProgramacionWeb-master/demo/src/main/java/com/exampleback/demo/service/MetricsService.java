package com.exampleback.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.exampleback.demo.dto.MetricDefinitionDTO;
import com.exampleback.demo.dto.MetricResponseDTO;
import com.exampleback.demo.model.DeveloperMetric;
import com.exampleback.demo.model.MetricType;
import com.exampleback.demo.repository.DeveloperMetricRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final DeveloperMetricRepository repository;

    public List<MetricDefinitionDTO> getAvailableMetrics() {
        return MetricType.definitions();
    }

    public List<MetricResponseDTO> getMetricData(String metricKey) {
        MetricType metricType = MetricType.fromKey(metricKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Métrica no soportada: " + metricKey));

        List<DeveloperMetric> metrics = repository.findAllByOrderByMetricDateAsc();

        return metrics.stream()
                .map(metric -> {
                    MetricResponseDTO dto = new MetricResponseDTO();
                    dto.setLabel(metric.getMetricDate());
                    dto.setValue(metricType.extractValue(metric));
                    return dto;
                })
                .toList();
    }
}