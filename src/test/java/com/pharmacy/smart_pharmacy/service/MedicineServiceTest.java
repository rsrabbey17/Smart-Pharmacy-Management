package com.pharmacy.smart_pharmacy.service;

import com.pharmacy.smart_pharmacy.dto.request.MedicineRequest;
import com.pharmacy.smart_pharmacy.dto.response.MedicineResponse;
import com.pharmacy.smart_pharmacy.entity.Medicine;
import com.pharmacy.smart_pharmacy.exception.BadRequestException;
import com.pharmacy.smart_pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.smart_pharmacy.repository.CategoryRepository;
import com.pharmacy.smart_pharmacy.repository.MedicineRepository;
import com.pharmacy.smart_pharmacy.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

    @Mock private MedicineRepository medicineRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SupplierRepository supplierRepository;

    @InjectMocks private MedicineService medicineService;

    private Medicine medicine;
    private MedicineRequest medicineRequest;

    @BeforeEach
    void setUp() {
        medicine = Medicine.builder()
                .id(1L)
                .name("Amoxicillin 500mg")
                .genericName("Amoxicillin")
                .barcode("8801234567890")
                .price(new BigDecimal("12.50"))
                .quantity(100)
                .reorderLevel(20)
                .active(true)
                .build();

        medicineRequest = new MedicineRequest();
        medicineRequest.setName("Amoxicillin 500mg");
        medicineRequest.setGenericName("Amoxicillin");
        medicineRequest.setBarcode("8801234567890");
        medicineRequest.setPrice(new BigDecimal("12.50"));
        medicineRequest.setQuantity(100);
    }

    @Test
    void getAll_shouldReturnOnlyActiveMedicines() {
        when(medicineRepository.findByActiveTrue()).thenReturn(List.of(medicine));

        List<MedicineResponse> result = medicineService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Amoxicillin 500mg");
        verify(medicineRepository).findByActiveTrue();
    }

    @Test
    void getById_shouldReturnMedicine_whenExists() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));

        MedicineResponse response = medicineService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBarcode()).isEqualTo("8801234567890");
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(medicineRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicineService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medicine not found");
    }

    @Test
    void create_shouldSaveNewMedicine() {
        when(medicineRepository.findByBarcode("8801234567890")).thenReturn(Optional.empty());
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);

        MedicineResponse response = medicineService.create(medicineRequest);

        assertThat(response.getName()).isEqualTo("Amoxicillin 500mg");
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    void create_shouldThrow_whenBarcodeAlreadyExists() {
        when(medicineRepository.findByBarcode("8801234567890"))
                .thenReturn(Optional.of(medicine));

        assertThatThrownBy(() -> medicineService.create(medicineRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Barcode already exists");

        verify(medicineRepository, never()).save(any(Medicine.class));
    }

    @Test
    void update_shouldModifyExistingMedicine() {
        MedicineRequest updateReq = new MedicineRequest();
        updateReq.setName("Amoxicillin 250mg");
        updateReq.setBarcode("8801234567890");
        updateReq.setPrice(new BigDecimal("8.00"));
        updateReq.setQuantity(50);

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));
        when(medicineRepository.findByBarcode("8801234567890")).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);

        medicineService.update(1L, updateReq);

        assertThat(medicine.getName()).isEqualTo("Amoxicillin 250mg");
        assertThat(medicine.getPrice()).isEqualByComparingTo("8.00");
        assertThat(medicine.getQuantity()).isEqualTo(50);
    }

    @Test
    void delete_shouldSoftDelete_notHardDelete() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);

        medicineService.delete(1L);

        // Should have set active=false, NOT called delete()
        assertThat(medicine.isActive()).isFalse();
        verify(medicineRepository).save(medicine);
        verify(medicineRepository, never()).delete(any());
    }

    @Test
    void search_shouldReturnMatchingMedicines() {
        when(medicineRepository.searchByName("amox")).thenReturn(List.of(medicine));

        List<MedicineResponse> result = medicineService.search("amox");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Amoxicillin");
    }

    @Test
    void getLowStock_shouldReturnMedicinesBelowReorderLevel() {
        Medicine lowStock = Medicine.builder()
                .id(2L).name("Paracetamol").barcode("111")
                .price(new BigDecimal("5.00")).quantity(5).reorderLevel(20)
                .active(true).build();

        when(medicineRepository.findLowStockMedicines()).thenReturn(List.of(lowStock));

        List<MedicineResponse> result = medicineService.getLowStock();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(5);
    }
}