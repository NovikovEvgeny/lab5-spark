package com.novikov;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@Log
public class ApacheAccessLog implements Serializable {
    public static final String METHOD = "method";
    public static final String RESPONSE_CODE = "responseCode";
    public static final String DATE_STRING = "dateString";
    public static final String ENDPOINT = "endpoint";
    public static final String OUT_DATE_FORMAT = "dd/MMM/yyyy";

    // Example Apache log line:
    //   127.0.0.1 - - [21/Jul/2014:9:55:27 -0800] "GET /home.html HTTP/1.1" 200 2048
    private static final String LOG_ENTRY_PATTERN =
            // 1:IP  2:client 3:user 4:date time                   5:method 6:req 7:proto   8:respcode 9:size
            "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+)";
    private static final Pattern PATTERN = Pattern.compile(LOG_ENTRY_PATTERN);
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(OUT_DATE_FORMAT, Locale.US);

    private String method;
    private String endpoint;
    private String responseCode;
    private String dateString;

    public static ApacheAccessLog parseFromLogLine(String logline) {
        try {
            Matcher matcher = PATTERN.matcher(logline);
            if (!matcher.find()) {
                log.log(Level.ALL, "Cannot parse logline" + logline);
                throw new RuntimeException("Error parsing logline");
            }

            LocalDate logDateTime = LocalDate.parse(matcher.group(4), LOG_FORMATTER);

            return new ApacheAccessLog(matcher.group(5), matcher.group(6), matcher.group(8),
                    logDateTime.format(DATE_FORMATTER));
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getIntResponseCode() {
        try {
            return Integer.valueOf(responseCode);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
