package ru.netology.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class ResponseInfo {
    public static final String NEW_LINE = "\r\n";

    private final String protocol = "HTTP/1.1";
    private final int statusCode;
    private final String statusMessage;
    private final Map<String, String> headers;

    public ResponseInfo(HttpStatus httpStatus) {
        this(httpStatus.code, httpStatus.message);
    }

    public ResponseInfo(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = new HashMap<>();

        addHeader("Connection", "close");
        addHeader("Content-Length", "0");
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public Optional<String> getHeader(String name) {
        return ofNullable(headers.get(name));
    }

    public boolean hasContent() {
        return getHeader("Content-Length")
                .map(Integer::valueOf)
                .orElse(0) > 0;
    }

    public void setContentInfo(String contentType, long contentLength) {
        addHeader("Content-Type", contentType);
        addHeader("Content-Length", String.valueOf(contentLength));
    }

    public String build() {
        var sb = new StringBuilder();
        sb.append(getProtocol()).append(" ")
                .append(getStatusCode()).append(" ")
                .append(getStatusMessage()).append(NEW_LINE);
        headers.forEach((key, value) -> sb.append(key).append(": ").append(value).append(NEW_LINE));
        sb.append(NEW_LINE);

        return sb.toString();
    }

    public String getProtocol() {
        return protocol;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}
