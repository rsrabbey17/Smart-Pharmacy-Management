package com.pharmacy.smart_pharmacy.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private long totalMedicines;
    private long totalCustomers;
    private long totalSuppliers;
    private long lowStockCount;
    private long expiringSoonCount;
    private long todaySalesCount;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
}