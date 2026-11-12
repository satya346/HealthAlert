package com.healthalert.health_alert_backend.service;

import com.healthalert.health_alert_backend.model.EmergencyAlertRequest;
import com.healthalert.health_alert_backend.model.EmergencyContactRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmergencyAlertService {

    private static final Logger log =
            LoggerFactory.getLogger(EmergencyAlertService.class);

    private final WhatsAppCloudApiClient whatsAppCloudApiClient;

    public EmergencyAlertService(WhatsAppCloudApiClient whatsAppCloudApiClient) {
        this.whatsAppCloudApiClient = whatsAppCloudApiClient;
    }

    public void processAlert(EmergencyAlertRequest request) {

        String patientName = request.patientName() == null
                || request.patientName().isBlank()
                ? "Health Guardian user"
                : request.patientName();

        String recordedAt = request.recordedAt() == null
                || request.recordedAt().isBlank()
                ? OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : request.recordedAt();

        log.info(
                "Emergency alert received: type={}, bpm={}, contacts={}",
                request.alertType(),
                request.bpm(),
                request.contacts().size()
        );

        int accepted = 0;
        int failed = 0;

        for (EmergencyContactRequest contact : request.contacts()) {
            try {
                whatsAppCloudApiClient.sendEmergencyTemplate(
                        contact.phoneNumber(),
                        contact.name(),
                        patientName,
                        request.bpm(),
                        request.alertType(),
                        recordedAt,
                        request.latitude(),
                        request.longitude(),
                        request.locationAccuracyMeters()
                );
                accepted++;
            } catch (Exception ex) {
                failed++;
                log.error("Could not submit WhatsApp alert for contact={}: {}",
                        contact.name(), ex.getMessage());
            }
        }

        log.info("Emergency alert processing completed: accepted={}, failed={}",
                accepted, failed);
    }
}
