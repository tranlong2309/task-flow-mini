package com.taskflow.application.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportApplicationServiceTest {

    private final ReportApplicationService reportApplicationService = new ReportApplicationService(null, null, null, null);

    @Test
    void calculateCompletionRate_shouldReturnZero_whenTotalIsZero() {
        assertEquals(0.0, reportApplicationService.calculateCompletionRate(0, 0));
        assertEquals(0.0, reportApplicationService.calculateCompletionRate(10, 0)); // Edge case
    }

    @Test
    void calculateCompletionRate_shouldCalculateCorrectly() {
        assertEquals(14.3, reportApplicationService.calculateCompletionRate(4, 28));
        assertEquals(50.0, reportApplicationService.calculateCompletionRate(5, 10));
        assertEquals(100.0, reportApplicationService.calculateCompletionRate(10, 10));
        assertEquals(33.3, reportApplicationService.calculateCompletionRate(1, 3));
        assertEquals(66.7, reportApplicationService.calculateCompletionRate(2, 3));
    }
}
