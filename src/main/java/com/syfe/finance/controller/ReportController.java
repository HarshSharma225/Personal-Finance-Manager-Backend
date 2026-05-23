package com.syfe.finance.controller;

import com.syfe.finance.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> monthly(@PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<Map<String, Object>> yearly(@PathVariable int year) {
        return ResponseEntity.ok(reportService.getYearlyReport(year));
    }
}
