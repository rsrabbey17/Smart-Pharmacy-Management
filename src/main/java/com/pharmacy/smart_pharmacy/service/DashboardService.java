package com.pharmacy.smart_pharmacy.service;

import com.pharmacy.smart_pharmacy.dto.response.DashboardStats;
import com.pharmacy.smart_pharmacy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MedicineRepository medicineRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final SaleRepository saleRepository;

    public DashboardStats getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        return DashboardStats.builder()
                .totalMedicines(medicineRepository.count())
                .totalCustomers(customerRepository.count())
                .totalSuppliers(supplierRepository.count())
                .lowStockCount(medicineRepository.findLowStockMedicines().size())
                .expiringSoonCount(medicineRepository.findExpiringBefore(LocalDate.now().plusDays(30)).size())
                .todaySalesCount(saleRepository.countByCreatedAtBetween(todayStart, todayEnd))
                .todayRevenue(saleRepository.sumRevenueBetween(todayStart, todayEnd))
                .monthRevenue(saleRepository.sumRevenueBetween(monthStart, monthEnd))
                .build();
    }
}