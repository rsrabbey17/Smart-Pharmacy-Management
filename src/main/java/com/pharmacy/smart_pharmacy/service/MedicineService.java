package com.pharmacy.smart_pharmacy.service;

import com.pharmacy.smart_pharmacy.dto.request.MedicineRequest;
import com.pharmacy.smart_pharmacy.dto.response.MedicineResponse;
import com.pharmacy.smart_pharmacy.entity.Category;
import com.pharmacy.smart_pharmacy.entity.Medicine;
import com.pharmacy.smart_pharmacy.entity.Supplier;
import com.pharmacy.smart_pharmacy.exception.BadRequestException;
import com.pharmacy.smart_pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.smart_pharmacy.repository.CategoryRepository;
import com.pharmacy.smart_pharmacy.repository.MedicineRepository;
import com.pharmacy.smart_pharmacy.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    // ==================== READ ====================

    @Transactional(readOnly = true)
    public List<MedicineResponse> getAll() {
        return medicineRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicineResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> search(String keyword) {
        return medicineRepository.searchByName(keyword)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getLowStock() {
        return medicineRepository.findLowStockMedicines()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getExpiringSoon(int days) {
        return medicineRepository.findExpiringBefore(LocalDate.now().plusDays(days))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== WRITE ====================

    @Transactional
    public MedicineResponse create(MedicineRequest request) {
        if (medicineRepository.findByBarcode(request.getBarcode()).isPresent()) {
            throw new BadRequestException("Barcode already exists");
        }
        Medicine medicine = new Medicine();
        apply(medicine, request);
        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineRequest request) {
        Medicine medicine = findEntity(id);
        medicineRepository.findByBarcode(request.getBarcode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Barcode already used by another medicine");
            }
        });
        apply(medicine, request);
        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public void delete(Long id) {
        Medicine medicine = findEntity(id);
        medicine.setActive(false); // soft delete
        medicineRepository.save(medicine);
    }

    // ==================== HELPERS ====================

    public Medicine findEntity(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
    }

    private void apply(Medicine medicine, MedicineRequest req) {
        medicine.setName(req.getName());
        medicine.setGenericName(req.getGenericName());
        medicine.setBarcode(req.getBarcode());
        medicine.setManufacturer(req.getManufacturer());
        medicine.setBatchNumber(req.getBatchNumber());
        medicine.setPrice(req.getPrice());
        medicine.setCostPrice(req.getCostPrice());
        medicine.setQuantity(req.getQuantity());
        if (req.getReorderLevel() != null) {
            medicine.setReorderLevel(req.getReorderLevel());
        }
        medicine.setExpiryDate(req.getExpiryDate());
        medicine.setManufacturingDate(req.getManufacturingDate());
        medicine.setDosageForm(req.getDosageForm());
        medicine.setStrength(req.getStrength());
        medicine.setRequiresPrescription(req.isRequiresPrescription());
        medicine.setDescription(req.getDescription());

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            medicine.setCategory(category);
        }
        if (req.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(req.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
            medicine.setSupplier(supplier);
        }
    }

    public MedicineResponse toResponse(Medicine m) {
        return MedicineResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .genericName(m.getGenericName())
                .barcode(m.getBarcode())
                .manufacturer(m.getManufacturer())
                .price(m.getPrice())
                .quantity(m.getQuantity())
                .expiryDate(m.getExpiryDate())
                .dosageForm(m.getDosageForm())
                .strength(m.getStrength())
                .requiresPrescription(m.isRequiresPrescription())
                .categoryName(m.getCategory() != null ? m.getCategory().getName() : null)
                .supplierName(m.getSupplier() != null ? m.getSupplier().getName() : null)
                .active(m.isActive())
                .build();
    }
}