package ru.netology.http;

import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RequestReader {
    private final int EXPECTED_REQUEST_LINE_PARTS = 3;
    private final int EXPECTED_HEADER_LINE_PARTS = 2;

    private final int HEAD_LIMIT = 4096;
    private final int BODY_LIMIT = 10 * 1024 * 1024;

    protected Map<String, List<String>> parseUrlEncodedParams(String sourceString) {
        var pairList = URLEncodedUtils.parse(sourceString, UTF_8);
        var paramsMap = new HashMap<String, List<String>>();

        pairList.forEach(pair -> {
            paramsMap.computeIfAbsent(pair.getName(), k -> new ArrayList<>())
                    .add(pair.getValue());
        });

        return paramsMap;
    }

    public Request read(InputStream inputStream) throws IOException {
        try (var in = inputStream.markSupported()
                ? inputStream
                : new BufferedInputStream(inputStream)
        ) {
            in.mark(HEAD_LIMIT);

            final byte[] buf = new byte[HEAD_LIMIT];
            final var readCount = in.read(buf);

            // Find Request Line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buf, requestLineDelimiter, 0, readCount);
            if (requestLineEnd == -1) return null;

            // Extract Request Line
            final var requestLineParts = new String(Arrays.copyOf(buf, requestLineEnd)).split(" ");
            if (requestLineParts.length != EXPECTED_REQUEST_LINE_PARTS) return null;

            final var method = requestLineParts[0];
            final var pathParts = requestLineParts[1].split("[?#]");
            final var protocol = requestLineParts[2];

            final var path = pathParts[0];
            final var query = pathParts.length > 1 ? pathParts[1] : "";
            final var queryParams = parseUrlEncodedParams(query);

            // Find Headers
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buf, headersDelimiter, headersStart, readCount);
            if (headersEnd == -1)
                return new Request(method, path, protocol, queryParams, null, null);

            // Extract Headers
            in.reset();
            in.skip(headersStart);
            final var headersLines = new String(in.readNBytes(headersEnd - headersStart)).split("\r\n");

            Map<String, String> headers = new HashMap<>();
            for (String line : headersLines) {
                var headerParts = line.split(":");
                if (headerParts.length != EXPECTED_HEADER_LINE_PARTS) continue;
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }

            final var contentLength = parseInt(headers.getOrDefault("Content-Length", "0"));
            if (contentLength > BODY_LIMIT) return null;

            // no body with GET method
            if ("GET".equals(method) || contentLength == 0) {
                return new Request(method, path, protocol, queryParams, headers, null);
            }

            // Extract Body after headers delimiter
            final var bodyBytes = new byte[contentLength];
            in.skip(headersDelimiter.length);
            in.read(bodyBytes);

            return new Request(method, path, protocol, queryParams, headers, bodyBytes);
        }
    }

    public Request read(byte[] content) {
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        try {
            return read(in);
        } catch (IOException ignore) {
        }
        return null;
    }

    // from google guava with modifications
    protected static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
