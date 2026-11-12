package com.healthalert.health_alert_backend.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EmergencyAlertRequest(

        @NotNull(message = "Alert type is required")
        AlertType alertType,

        @Min(value = 1, message = "BPM must be greater than 0")
        @Max(value = 300, message = "Invalid BPM")
        int bpm,

        @NotEmpty(message = "At least one emergency contact is required")
        List<@Valid EmergencyContactRequest> contacts,

        // Optional fields. Existing Android requests remain compatible.
        String patientName,

        String recordedAt,
        Double latitude,
        Double longitude,
        Float locationAccuracyMeters,
        String locationCapturedAt,
        Boolean locationAvailable
) {
}
