package com.pharmacy.smart_pharmacy.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    private Long customerId;

    @NotEmpty
    private List<SaleItemRequest> items;

    private BigDecimal discountAmount;
    private String paymentMethod;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemRequest {
        @NotNull
        private Long medicineId;

        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
