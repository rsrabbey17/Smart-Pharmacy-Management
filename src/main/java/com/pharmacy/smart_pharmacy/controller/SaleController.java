package com.pharmacy.smart_pharmacy.controller;

import com.pharmacy.smart_pharmacy.dto.request.SaleRequest;
import com.pharmacy.smart_pharmacy.dto.response.ApiResponse;
import com.pharmacy.smart_pharmacy.entity.Sale;
import com.pharmacy.smart_pharmacy.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Sale>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(saleService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Sale>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getById(id)));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<Sale>>> today() {
        return ResponseEntity.ok(ApiResponse.success(saleService.getTodaySales()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST','CASHIER')")
    public ResponseEntity<ApiResponse<Sale>> create(@Valid @RequestBody SaleRequest request,
                                                    Authentication authentication) {
        Sale sale = saleService.create(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Sale completed", sale));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<ApiResponse<Sale>> refund(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Sale refunded", saleService.refund(id)));
    }
}