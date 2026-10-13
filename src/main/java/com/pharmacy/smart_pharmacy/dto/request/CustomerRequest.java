package com.pharmacy.smart_pharmacy.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {
    @NotBlank
    private String name;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$")
    private String phone;

    @Email
    private String email;
    private String address;
}