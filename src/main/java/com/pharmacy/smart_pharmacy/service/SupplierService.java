package com.pharmacy.smart_pharmacy.service;

import com.pharmacy.smart_pharmacy.dto.request.SupplierRequest;
import com.pharmacy.smart_pharmacy.entity.Supplier;
import com.pharmacy.smart_pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.smart_pharmacy.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    @Transactional
    public Supplier create(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .licenseNumber(request.getLicenseNumber())
                .build();
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier update(Long id, SupplierRequest request) {
        Supplier supplier = getById(id);
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setLicenseNumber(request.getLicenseNumber());
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void delete(Long id) {
        supplierRepository.delete(getById(id));
    }
}