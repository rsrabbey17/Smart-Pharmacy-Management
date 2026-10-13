package com.pharmacy.smart_pharmacy.controller;

import com.pharmacy.smart_pharmacy.dto.response.ApiResponse;
import com.pharmacy.smart_pharmacy.dto.response.DashboardStats;
import com.pharmacy.smart_pharmacy.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> stats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStats()));
    }
}