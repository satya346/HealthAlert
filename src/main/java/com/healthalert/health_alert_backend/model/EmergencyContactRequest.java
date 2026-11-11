package com.healthalert.health_alert_backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmergencyContactRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+[1-9]\\d{9,14}$",
                message = "Phone number must be in international format"
        )
        String phoneNumber
) {
}