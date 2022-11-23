package ru.netology.http;

import ru.netology.http.codec.FormUrlEncodedDecoder;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class RequestReader implements Closeable {
    protected static final int NOT_FOUND = -1;

    private final int EXPECTED_REQUEST_LINE_PARTS = 3;
    private final int EXPECTED_HEADER_LINE_PARTS = 2;

    private final int HEAD_LIMIT = 4096;
    private final int DEFAULT_BODY_LIMIT = 10 * 1024 * 1024;

    protected final InputStream in;
    protected final byte[] buffer;
    protected int bufferLimit;
    private int bodyLimit = DEFAULT_BODY_LIMIT;

    protected final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
    protected final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};

    protected int requestLineEnd = NOT_FOUND;
    protected int headersEnd = NOT_FOUND;

    protected String method;
    protected String path;
    protected String protocol;
    protected Map<String, List<String>> queryParams;
    protected Map<String, String> headers;
    protected byte[] bodyBytes;


    public RequestReader(InputStream inputStream) {
        this.in = inputStream.markSupported()
                ? inputStream
                : new BufferedInputStream(inputStream);
        in.mark(HEAD_LIMIT);
        buffer = new byte[HEAD_LIMIT];
    }

    public Request read() throws IOException {
        bufferLimit = in.read(buffer);

        if (readRequestLine() == NOT_FOUND)
            return null;

        if (readHeaders() == NOT_FOUND)
            return new Request(method, path, protocol, queryParams, null, null);

        if (getContentLength() > getBodyLimit()) return null;

        if (readBody() == NOT_FOUND)
            return new Request(method, path, protocol, queryParams, headers, null);

        return new Request(method, path, protocol, queryParams, headers, bodyBytes);
    }

    public static Request read(byte[] content) {
        try (RequestReader requestReader = new RequestReader(new ByteArrayInputStream(content))) {
            return requestReader.read();
        } catch (IOException ignore) {
        }
        return null;
    }

    protected int readRequestLine() {
        // Find Request Line
        requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, bufferLimit);
        if (requestLineEnd == NOT_FOUND) return NOT_FOUND;

        // Extract Request Line
        final var requestLineParts = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLineParts.length != EXPECTED_REQUEST_LINE_PARTS) return NOT_FOUND;

        method = requestLineParts[0];
        final var pathParts = requestLineParts[1].split("[?#]");
        protocol = requestLineParts[2];

        path = pathParts[0];
        final var query = pathParts.length > 1 ? pathParts[1] : "";
        queryParams = FormUrlEncodedDecoder.parseUrlEncodedParams(query);

        return requestLineEnd;
    }

    protected int readHeaders() throws IOException {
        // Find Headers
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        headersEnd = indexOf(buffer, headersDelimiter, headersStart, bufferLimit);
        if (headersEnd == NOT_FOUND) return NOT_FOUND;

        // Extract Headers
        in.reset();
        in.skip(headersStart);
        final var headersLines = new String(in.readNBytes(headersEnd - headersStart)).split("\r\n");

        headers = new HashMap<>();
        for (String line : headersLines) {
            var headerParts = line.split(":");
            if (headerParts.length != EXPECTED_HEADER_LINE_PARTS) continue;
            headers.put(headerParts[0].trim(), headerParts[1].trim());
        }

        return headersEnd;
    }

    protected int readBody() throws IOException {
        final var contentLength = getContentLength();

        // no body with GET method
        if ("GET".equals(method) || contentLength == 0) return NOT_FOUND;

        // Extract Body after headers delimiter
        bodyBytes = new byte[contentLength];
        in.skip(headersDelimiter.length);
        in.read(bodyBytes);

        return contentLength;
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
        return NOT_FOUND;
    }

    public int getBodyLimit() {
        return bodyLimit;
    }

    public void setBodyLimit(int bodyLimit) {
        this.bodyLimit = bodyLimit;
    }

    protected int getContentLength() {
        return headers == null ? 0 : parseInt(headers.getOrDefault("Content-Length", "0"));
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
