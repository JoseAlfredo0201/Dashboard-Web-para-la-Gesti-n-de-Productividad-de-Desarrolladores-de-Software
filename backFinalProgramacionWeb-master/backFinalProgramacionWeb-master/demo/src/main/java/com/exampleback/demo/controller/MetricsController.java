package com.exampleback.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exampleback.demo.dto.MetricDefinitionDTO;
import com.exampleback.demo.dto.MetricResponseDTO;
import com.exampleback.demo.service.MetricsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService service;

    @GetMapping
    public List<MetricDefinitionDTO> getAvailableMetrics() {
        return service.getAvailableMetrics();
    }

    @GetMapping("/{metric}")
    public List<MetricResponseDTO> getMetricData(
            @org.springframework.web.bind.annotation.PathVariable String metric) {

        return service.getMetricData(metric);
    }
}