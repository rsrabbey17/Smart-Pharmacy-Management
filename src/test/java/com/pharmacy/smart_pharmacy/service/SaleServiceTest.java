package com.pharmacy.smart_pharmacy.service;

import com.pharmacy.smart_pharmacy.dto.request.SaleRequest;
import com.pharmacy.smart_pharmacy.entity.*;
import com.pharmacy.smart_pharmacy.exception.BadRequestException;
import com.pharmacy.smart_pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.smart_pharmacy.repository.CustomerRepository;
import com.pharmacy.smart_pharmacy.repository.MedicineRepository;
import com.pharmacy.smart_pharmacy.repository.SaleRepository;
import com.pharmacy.smart_pharmacy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock private SaleRepository saleRepository;
    @Mock private MedicineRepository medicineRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private SaleService saleService;

    private User cashier;
    private Medicine medicine;
    private SaleRequest saleRequest;

    @BeforeEach
    void setUp() {
        cashier = User.builder()
                .id(1L).username("admin").role(Role.ADMIN).enabled(true).build();

        medicine = Medicine.builder()
                .id(1L)
                .name("Paracetamol 500mg")
                .barcode("12345")
                .price(new BigDecimal("5.00"))
                .quantity(100)
                .expiryDate(LocalDate.now().plusYears(1))
                .active(true)
                .build();

        SaleRequest.SaleItemRequest itemReq = new SaleRequest.SaleItemRequest();
        itemReq.setMedicineId(1L);
        itemReq.setQuantity(3);

        saleRequest = new SaleRequest();
        saleRequest.setItems(List.of(itemReq));
        saleRequest.setDiscountAmount(new BigDecimal("1.00"));
        saleRequest.setPaymentMethod("CASH");
    }

    @Test
    void create_shouldSucceed_whenStockIsSufficient() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(cashier));
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = saleService.create(saleRequest, "admin");

        assertThat(sale).isNotNull();
        assertThat(sale.getInvoiceNumber()).startsWith("INV-");
        assertThat(sale.getStatus()).isEqualTo("COMPLETED");
        assertThat(sale.getPaymentMethod()).isEqualTo("CASH");

        // Subtotal = 5.00 * 3 = 15.00, minus discount 1.00 = 14.00, + 5% tax = 14.70
        assertThat(sale.getSubtotal()).isEqualByComparingTo("15.00");
        assertThat(sale.getDiscountAmount()).isEqualByComparingTo("1.00");
        assertThat(sale.getTotalAmount()).isEqualByComparingTo("14.70");
    }

    @Test
    void create_shouldDecrementStock() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(cashier));
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        saleService.create(saleRequest, "admin");

        // Was 100, sold 3
        assertThat(medicine.getQuantity()).isEqualTo(97);
        verify(medicineRepository).save(medicine);
    }

    @Test
    void create_shouldThrow_whenInsufficientStock() {
        medicine.setQuantity(2); // only 2 left, trying to sell 3

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(cashier));
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));

        assertThatThrownBy(() -> saleService.create(saleRequest, "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");

        verify(saleRepository, never()).save(any(Sale.class));
        verify(medicineRepository, never()).save(any(Medicine.class));
    }

    @Test
    void create_shouldThrow_whenMedicineExpired() {
        medicine.setExpiryDate(LocalDate.now().minusDays(1));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(cashier));
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));

        assertThatThrownBy(() -> saleService.create(saleRequest, "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void create_shouldThrow_whenMedicineNotFound() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(cashier));
        when(medicineRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.create(saleRequest, "admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Medicine not found");
    }

    @Test
    void refund_shouldRestoreStock() {
        Sale sale = Sale.builder()
                .id(1L).invoiceNumber("INV-ABC")
                .status("COMPLETED").totalAmount(new BigDecimal("14.70"))
                .build();

        SaleItem item = SaleItem.builder()
                .medicine(medicine).quantity(3)
                .unitPrice(new BigDecimal("5.00"))
                .lineTotal(new BigDecimal("15.00"))
                .build();
        sale.addItem(item);

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        int qtyBefore = medicine.getQuantity();
        Sale refunded = saleService.refund(1L);

        assertThat(refunded.getStatus()).isEqualTo("REFUNDED");
        assertThat(medicine.getQuantity()).isEqualTo(qtyBefore + 3);
    }

    @Test
    void refund_shouldThrow_whenAlreadyRefunded() {
        Sale sale = Sale.builder().id(1L).status("REFUNDED").build();
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.refund(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already refunded");
    }
}