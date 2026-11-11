# WhatsApp Cloud API integration

The backend now submits each emergency contact notification through Meta's WhatsApp Cloud API.

## 1. Configure environment variables

Do not put the access token in source control or in the Android app. Set these environment variables in your local shell or deployment platform:

| Environment variable | Value |
|---|---|
| `WHATSAPP_ENABLED` | `true` to enable sending; defaults to `false` |
| `WHATSAPP_API_VERSION` | Graph API version shown in Meta for Developers, e.g. `vXX.0` |
| `WHATSAPP_PHONE_NUMBER_ID` | Phone Number ID from WhatsApp > API Setup (not the displayed test phone number) |
| `WHATSAPP_ACCESS_TOKEN` | Meta access token |
| `WHATSAPP_TEMPLATE_NAME` | `hello_world` for the initial test |
| `WHATSAPP_TEMPLATE_LANGUAGE` | `en_US` for the initial test |
| `WHATSAPP_TEMPLATE_BODY_PARAMETERS_ENABLED` | `false` for `hello_world` |

Replace `vXX.0` with the actual version displayed by Meta. Do not copy the placeholder literally.

### Windows PowerShell example

```powershell
$env:WHATSAPP_ENABLED="true"
$env:WHATSAPP_API_VERSION="vXX.0"
$env:WHATSAPP_PHONE_NUMBER_ID="YOUR_PHONE_NUMBER_ID"
$env:WHATSAPP_ACCESS_TOKEN="YOUR_ACCESS_TOKEN"
$env:WHATSAPP_TEMPLATE_NAME="hello_world"
$env:WHATSAPP_TEMPLATE_LANGUAGE="en_US"
$env:WHATSAPP_TEMPLATE_BODY_PARAMETERS_ENABLED="false"

.\mvnw.cmd spring-boot:run
```

Use your actual Graph API version and credentials. Never share the access token in chat, screenshots, source files, or Git.

## 2. Test the existing endpoint

The existing endpoint is retained:

```http
POST http://localhost:8080/api/emergency-alerts
Content-Type: application/json
```

Example body (the phone number must be a recipient verified/allowed in your Meta test setup):

```json
{
  "alertType": "LOW",
  "bpm": 42,
  "patientName": "Health Guardian user",
  "recordedAt": "2026-11-11T21:30:00+05:30",
  "contacts": [
    {
      "name": "Emergency Contact",
      "phoneNumber": "+919876543210"
    }
  ]
}
```

For Meta's `hello_world` test template, the backend sends the template without body parameters. The `patientName`, BPM and alert type are accepted by the backend but are not displayed by `hello_world`.

A successful Graph API response means Meta accepted the message request. It does not guarantee delivery. Delivery status webhooks are a separate next step.

## 3. Configure the Health Guardian template

After Meta approves your own template, set:

```powershell
$env:WHATSAPP_TEMPLATE_NAME="health_alert"
$env:WHATSAPP_TEMPLATE_LANGUAGE="en_US"
$env:WHATSAPP_TEMPLATE_BODY_PARAMETERS_ENABLED="true"
```

The implementation sends five body parameters in this order:

1. Contact name
2. Patient name
3. Heart rate (BPM)
4. Alert type (`High heart rate` or `Low heart rate`)
5. Recorded timestamp

Your approved Meta template must contain exactly five matching body placeholders (`{{1}}` through `{{5}}`) in that order. If the approved template differs, update the parameter mapping before enabling it.

## 4. Important behavior and production checklist

- WhatsApp sending is disabled by default. Enable it only after setting valid credentials.
- Each contact is sent an individual WhatsApp message; this is not a WhatsApp group message.
- Meta's test number can message only recipient numbers added and verified in the Meta API Setup screen.
- The recipient phone number is normalized to digits-only international format before sending.
- One recipient failure does not stop attempts to notify the remaining contacts.
- The existing controller returns HTTP 202 after processing. Check backend logs for per-recipient submission failures.
- Add Meta webhook verification/signature validation and delivery-status persistence before production.
- Obtain explicit opt-in from each emergency contact. Protect the endpoint with authentication/rate limiting and avoid exposing tokens.
- Heart-rate thresholds and alerts are not a medical diagnosis. Provide appropriate emergency guidance in the app.
