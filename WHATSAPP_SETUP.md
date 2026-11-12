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
| `WHATSAPP_TEMPLATE_NAME` | `health_guardian_emergency` for the currently approved Health Guardian template |
| `WHATSAPP_TEMPLATE_LANGUAGE` | `en` (confirm the exact language code for this template in Meta) |
| `WHATSAPP_TEMPLATE_BODY_PARAMETERS_ENABLED` | `true` for the two approved body variables |

Replace `vXX.0` with the actual version displayed by Meta. Do not copy the placeholder literally.

### Windows PowerShell example

```powershell
$env:WHATSAPP_ENABLED="true"
$env:WHATSAPP_API_VERSION="vXX.0"
$env:WHATSAPP_PHONE_NUMBER_ID="YOUR_PHONE_NUMBER_ID"
$env:WHATSAPP_ACCESS_TOKEN="YOUR_ACCESS_TOKEN"
$env:WHATSAPP_TEMPLATE_NAME="health_guardian_emergency"
$env:WHATSAPP_TEMPLATE_LANGUAGE="en"
$env:WHATSAPP_TEMPLATE_BODY_PARAMETERS_ENABLED="true"

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

## 3. Currently approved Health Guardian template mapping

The currently approved `health_guardian_emergency` template shown in WhatsApp Manager has:
- Static TEXT header: `Health Alert` (do not send a header component)
- Body `{{1}}`: patient name
- Body `{{2}}`: recorded timestamp
- Footer: `Health Alert – Automated Notification`
- No URL button and no LOCATION header

The backend therefore sends exactly two BODY text parameters, in this order: patient name, recorded timestamp. It deliberately does not send a location header. Although Android may capture and submit GPS coordinates, this particular approved template has no location URL button, so the coordinates are omitted from the WhatsApp message until a suitable template is approved. Do not map coordinates into the timestamp variable.

The template language configured in Render must match the exact language code shown by Meta for this template. The default in this package is `en`; if Meta's API details show a different code, use that exact value.

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
