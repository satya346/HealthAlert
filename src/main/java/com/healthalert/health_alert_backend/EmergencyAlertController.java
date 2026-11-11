package com.healthalert.health_alert_backend;

import com.healthalert.health_alert_backend.model.EmergencyAlertRequest;
import com.healthalert.health_alert_backend.service.EmergencyAlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergency-alerts")
public class EmergencyAlertController {

    private final EmergencyAlertService emergencyAlertService;

    public EmergencyAlertController(
            EmergencyAlertService emergencyAlertService
    ) {
        this.emergencyAlertService = emergencyAlertService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveAlert(
            @Valid @RequestBody EmergencyAlertRequest request
    ) {

        emergencyAlertService.processAlert(request);

        return ResponseEntity.accepted().build();
    }
}