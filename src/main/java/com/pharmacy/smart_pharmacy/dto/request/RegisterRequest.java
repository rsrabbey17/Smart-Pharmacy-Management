package com.pharmacy.smart_pharmacy.dto.request;

import com.pharmacy.smart_pharmacy.entity.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100)
    private String password;

    private String fullName;

    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Invalid phone")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;
}
