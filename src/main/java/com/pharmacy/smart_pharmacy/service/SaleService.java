package com.pharmacy.smart_pharmacy.service;

import com.pharmacy.smart_pharmacy.dto.request.SaleRequest;
import com.pharmacy.smart_pharmacy.entity.*;
import com.pharmacy.smart_pharmacy.exception.BadRequestException;
import com.pharmacy.smart_pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.smart_pharmacy.repository.CustomerRepository;
import com.pharmacy.smart_pharmacy.repository.MedicineRepository;
import com.pharmacy.smart_pharmacy.repository.SaleRepository;
import com.pharmacy.smart_pharmacy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final MedicineRepository medicineRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");

    @Transactional(readOnly = true)
    public List<Sale> getAll() {
        return saleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Sale getById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
    }

    @Transactional
    public Sale create(SaleRequest request, String cashierUsername) {
        User cashier = userRepository.findByUsername(cashierUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Cashier not found"));

        Sale sale = new Sale();
        sale.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        sale.setCashier(cashier);

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            sale.setCustomer(customer);
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (SaleRequest.SaleItemRequest itemReq : request.getItems()) {
            Medicine medicine = medicineRepository.findById(itemReq.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine not found: " + itemReq.getMedicineId()));

            if (medicine.getQuantity() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + medicine.getName()
                        + ". Available: " + medicine.getQuantity());
            }

            if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Medicine expired: " + medicine.getName());
            }

            BigDecimal lineTotal = medicine.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            SaleItem item = SaleItem.builder()
                    .medicine(medicine)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(medicine.getPrice())
                    .lineTotal(lineTotal)
                    .build();
            sale.addItem(item);

            // Decrement stock
            medicine.setQuantity(medicine.getQuantity() - itemReq.getQuantity());
            medicineRepository.save(medicine);

            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal tax = subtotal.subtract(discount).multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discount).add(tax);

        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(discount);
        sale.setTaxAmount(tax);
        sale.setTotalAmount(total);
        sale.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
        sale.setStatus("COMPLETED");

        Sale saved = saleRepository.save(sale);

        // Loyalty points (1 point per 10 currency units)
        if (saved.getCustomer() != null) {
            Customer c = saved.getCustomer();
            int points = total.divide(BigDecimal.TEN, 0, RoundingMode.DOWN).intValue();
            c.setLoyaltyPoints(c.getLoyaltyPoints() + points);
            customerRepository.save(c);
        }

        return saved;
    }

    @Transactional
    public Sale refund(Long id) {
        Sale sale = getById(id);
        if ("REFUNDED".equals(sale.getStatus())) {
            throw new BadRequestException("Sale already refunded");
        }
        // Restore stock
        for (SaleItem item : sale.getItems()) {
            Medicine m = item.getMedicine();
            m.setQuantity(m.getQuantity() + item.getQuantity());
            medicineRepository.save(m);
        }
        sale.setStatus("REFUNDED");
        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<Sale> getTodaySales() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return saleRepository.findByCreatedAtBetween(start, end);
    }
}
