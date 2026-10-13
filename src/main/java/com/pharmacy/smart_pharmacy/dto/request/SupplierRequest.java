package com.pharmacy.smart_pharmacy.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {
    @NotBlank
    private String name;
    private String contactPerson;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$")
    private String phone;

    @Email
    private String email;
    private String address;
    private String licenseNumber;
}