package com.exampleback.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.exampleback.demo.model.DeveloperMetric;
import com.exampleback.demo.repository.DeveloperMetricRepository;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private DeveloperMetricRepository repository;

    @InjectMocks
    private MetricsService service;

    @Test
    void shouldMapCommitMetricSeries() {
        when(repository.findAllByOrderByMetricDateAsc()).thenReturn(List.of(
            new DeveloperMetric("Ana", "2026-05-01", 4, 1, 2, 6),
            new DeveloperMetric("Ana", "2026-05-02", 7, 0, 3, 8)
        ));

        var series = service.getMetricData("commits");

        assertEquals(2, series.size());
        assertEquals("2026-05-01", series.get(0).getLabel());
        assertEquals(4, series.get(0).getValue());
        assertEquals(7, series.get(1).getValue());
    }

    @Test
    void shouldRejectUnknownMetric() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getMetricData("invalid"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}