package com.healthalert.health_alert_backend.service;

import com.healthalert.health_alert_backend.model.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WhatsAppCloudApiClient {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsAppCloudApiClient.class);

    private final RestClient restClient;
    private final String apiVersion;
    private final String phoneNumberId;
    private final String accessToken;
    private final String templateName;
    private final String templateLanguage;
    private final boolean enabled;
    private final boolean templateBodyParametersEnabled;

    public WhatsAppCloudApiClient(
            @Value("${whatsapp.api.base-url:https://graph.facebook.com}") String apiBaseUrl,
            @Value("${whatsapp.api.version:}") String apiVersion,
            @Value("${whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${whatsapp.access-token:}") String accessToken,
            @Value("${whatsapp.template-name:hello_world}") String templateName,
            @Value("${whatsapp.template-language:en_US}") String templateLanguage,
            @Value("${whatsapp.enabled:false}") boolean enabled,
            @Value("${whatsapp.template-body-parameters-enabled:false}") boolean templateBodyParametersEnabled) {

        this.restClient = RestClient.builder()
                .baseUrl(apiBaseUrl)
                .build();
        this.apiVersion = apiVersion;
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
        this.templateName = templateName;
        this.templateLanguage = templateLanguage;
        this.enabled = enabled;
        this.templateBodyParametersEnabled = templateBodyParametersEnabled;
    }

    public void sendEmergencyTemplate(
            String recipientPhoneNumber,
            String contactName,
            String patientName,
            int bpm,
            AlertType alertType,
            String recordedAt) {

        if (!enabled) {
            log.info("WhatsApp integration is disabled. Skipping recipient={}",
                    maskPhoneNumber(recipientPhoneNumber));
            return;
        }

        validateConfiguration();

        Map<String, Object> template = new LinkedHashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", templateLanguage));

        if (templateBodyParametersEnabled) {
            List<Map<String, String>> parameters = new ArrayList<>();
            parameters.add(textParameter(contactName));
            parameters.add(textParameter(patientName));
            parameters.add(textParameter(String.valueOf(bpm)));
            parameters.add(textParameter(alertType == AlertType.HIGH
                    ? "High heart rate" : "Low heart rate"));
            parameters.add(textParameter(recordedAt));

            template.put("components", List.of(
                    Map.of("type", "body", "parameters", parameters)
            ));
        }

        Map<String, Object> requestBody = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", normalizePhoneNumber(recipientPhoneNumber),
                "type", "template",
                "template", template
        );

        try {
            String response = restClient.post()
                    .uri("/{version}/{phoneNumberId}/messages",
                            apiVersion, phoneNumberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("WhatsApp API accepted emergency message for recipient={}; response={}",
                    maskPhoneNumber(recipientPhoneNumber), response);
        } catch (RestClientException ex) {
            log.error("WhatsApp API request failed for recipient={}: {}",
                    maskPhoneNumber(recipientPhoneNumber), ex.getMessage());
            throw ex;
        }
    }

    private void validateConfiguration() {
        if (apiVersion == null || apiVersion.isBlank()
                || phoneNumberId == null || phoneNumberId.isBlank()
                || accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException(
                    "WhatsApp is enabled but API version, phone number ID or access token is missing");
        }
        if (templateName == null || templateName.isBlank()
                || templateLanguage == null || templateLanguage.isBlank()) {
            throw new IllegalStateException(
                    "WhatsApp template name and language must be configured");
        }
    }

    private Map<String, String> textParameter(String value) {
        return Map.of("type", "text", "text",
                value == null || value.isBlank() ? "Not available" : value);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 5) {
            return "****";
        }
        return "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
