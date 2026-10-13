package com.pharmacy.smart_pharmacy.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequest {

    @NotBlank
    private String name;

    private String genericName;

    @NotBlank
    private String barcode;

    private String manufacturer;
    private String batchNumber;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    private BigDecimal costPrice;

    @NotNull
    @Min(0)
    private Integer quantity;

    private Integer reorderLevel;
    private LocalDate expiryDate;
    private LocalDate manufacturingDate;
    private String dosageForm;
    private String strength;
    private boolean requiresPrescription;
    private String description;
    private Long categoryId;
    private Long supplierId;
}
