package com.jlh.jlhautopambackend.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class IcsUtil {
    private IcsUtil() {}

    private static final DateTimeFormatter ICS =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    public static String fmt(Instant i) { return ICS.format(i); }

    public static String uid(String seed) {
        try {
            var md = MessageDigest.getInstance("SHA-1");
            var dig = md.digest(seed.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb + "@jlh-autopam.local";
        } catch (Exception e) {
            return seed + "@jlh-autopam.local";
        }
    }

    // Échappement minimal RFC5545
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    /**
     * VEVENT avec deux alarmes: -24h et -1h (DISPLAY).
     * Ajoute ORGANIZER si organizerEmail non nul/blank.
     */
    public static String veventWithAlarms(String uid,
                                          String summary,
                                          String description,
                                          String location,
                                          Instant startUtc,
                                          Instant endUtc,
                                          String organizerCommonName,   // ex: "JLH Auto Pam"
                                          String organizerEmail // ex: "contact@jlh-autopam.fr" (optionnel)
    ) {
        StringBuilder sb = new StringBuilder(640);
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("PRODID:-//JLH Auto Pam//RendezVous//FR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(fmt(Instant.now())).append("\r\n");
        sb.append("DTSTART:").append(fmt(startUtc)).append("\r\n");
        sb.append("DTEND:").append(fmt(endUtc)).append("\r\n");
        if (summary != null && !summary.isBlank())
            sb.append("SUMMARY:").append(esc(summary)).append("\r\n");
        if (description != null && !description.isBlank())
            sb.append("DESCRIPTION:").append(esc(description)).append("\r\n");
        if (location != null && !location.isBlank())
            sb.append("LOCATION:").append(esc(location)).append("\r\n");

        // ORGANIZER (optionnel)
        if (organizerEmail != null && !organizerEmail.isBlank()) {
            String cn = (organizerCommonName != null && !organizerCommonName.isBlank())
                    ? organizerCommonName : "JLH Auto Pam";
            sb.append("ORGANIZER;CN=").append(esc(cn))
                    .append(":MAILTO:").append(esc(organizerEmail)).append("\r\n");
        }

        // VALARM -24h
        sb.append("BEGIN:VALARM\r\n");
        sb.append("ACTION:DISPLAY\r\n");
        sb.append("DESCRIPTION:").append(esc(summary != null ? summary : "Rappel")).append("\r\n");
        sb.append("TRIGGER:-PT24H\r\n");
        sb.append("END:VALARM\r\n");

        // VALARM -1h
        sb.append("BEGIN:VALARM\r\n");
        sb.append("ACTION:DISPLAY\r\n");
        sb.append("DESCRIPTION:").append(esc(summary != null ? summary : "Rappel")).append("\r\n");
        sb.append("TRIGGER:-PT1H\r\n");
        sb.append("END:VALARM\r\n");

        sb.append("END:VEVENT\r\n");
        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    // Alias pour compat (si des appels existaient déjà)
    public static String buildVEvent(String uid,
                                     String summary,
                                     String description,
                                     String location,
                                     Instant startUtc,
                                     Instant endUtc) {
        return veventWithAlarms(uid, summary, description, location, startUtc, endUtc, null, null);
    }
}
