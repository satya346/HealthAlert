# Health Guardian location-enabled WhatsApp alerts

## Android permissions
The app declares ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION,
ACCESS_BACKGROUND_LOCATION and FOREGROUND_SERVICE_LOCATION. The SOS test
requests foreground location permission at runtime. Automatic background
location requires the user to separately grant "Allow all the time" in
Android App info > Permissions > Location. Android may require the user to
enable this from system settings; do not silently request it.

The health monitoring service declares `health|location` foreground service
types. Verify the device/target SDK policy and Play Console declarations
before publishing. If background location is not granted or a fix cannot be
obtained, the alert is still sent without coordinates.

## Meta WhatsApp template compatibility
The currently approved `health_guardian_emergency` template has a static TEXT
header and two BODY variables; it has no LOCATION header and no URL button.
The backend intentionally omits GPS coordinates from this template payload to
avoid Meta error 132012. Android can still capture and submit the location, but
this template cannot deliver it to contacts. To share a Google Maps link, first
obtain approval for a template with a dynamic URL button, then add its exact
button component and parameter mapping to the backend. Do not repurpose the
existing timestamp variable for coordinates.

A location header is a separate template format and must not be sent to a
TEXT-header template. Any location included in a message is a point-in-time
snapshot, not continuous tracking. Do not claim an exact location when GPS
accuracy is poor.

## Render environment
Keep `WHATSAPP_ACCESS_TOKEN` in Render secrets/environment variables.
Configure the approved template name and language. If the template has
five body placeholders, enable body parameters and ensure the template
placeholder order matches the backend:
contact name, patient name, BPM, alert type, recorded time.

## Privacy and safety
Request location only with clear user consent. Explain that coordinates are
shared with the user's saved emergency contacts. Avoid logging raw
coordinates or access tokens. Provide a no-location fallback and never delay
an alert indefinitely waiting for GPS.
