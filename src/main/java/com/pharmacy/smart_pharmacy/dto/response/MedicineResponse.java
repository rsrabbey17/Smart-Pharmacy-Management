package com.pharmacy.smart_pharmacy.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineResponse {
    private Long id;
    private String name;
    private String genericName;
    private String barcode;
    private String manufacturer;
    private BigDecimal price;
    private Integer quantity;
    private LocalDate expiryDate;
    private String dosageForm;
    private String strength;
    private boolean requiresPrescription;
    private String categoryName;
    private String supplierName;
    private boolean active;
}