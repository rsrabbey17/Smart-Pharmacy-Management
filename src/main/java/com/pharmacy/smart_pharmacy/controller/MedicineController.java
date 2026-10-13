package com.pharmacy.smart_pharmacy.controller;

import com.pharmacy.smart_pharmacy.dto.request.MedicineRequest;
import com.pharmacy.smart_pharmacy.dto.response.ApiResponse;
import com.pharmacy.smart_pharmacy.dto.response.MedicineResponse;
import com.pharmacy.smart_pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success(medicineService.search(keyword)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> lowStock() {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getLowStock()));
    }

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> expiring(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getExpiringSoon(days)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineResponse>> create(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Medicine created", medicineService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Medicine updated", medicineService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        medicineService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Medicine deleted", null));
    }
}