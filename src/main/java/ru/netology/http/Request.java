package ru.netology.http;

import java.util.*;

import static java.util.Optional.ofNullable;

public class Request {
    private final String protocol;
    private final String path;
    private final String method;
    private final Map<String, List<String>> queryParams;
    private final Map<String, String> headers;
    private final byte[] body;

    public Request(String method, String path, String protocol) {
        this(method, path, protocol, null, null, null);
    }

    public Request(
            String method,
            String path,
            String protocol,
            Map<String, List<String>> queryParams,
            Map<String, String> headers,
            byte[] body
    ) {
        this.method = method.toUpperCase();
        this.path = path;
        this.queryParams = ofNullable(queryParams).orElseGet(HashMap::new);
        this.protocol = protocol;
        this.body = ofNullable(body).orElseGet(() -> new byte[0]);
        this.headers = ofNullable(headers).orElseGet(HashMap::new);
    }

    public Optional<String> getHeader(String name) {
        return ofNullable(headers.get(name));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Request.class.getSimpleName() + "[", "]")
                .add("protocol='" + protocol + "'")
                .add("method='" + method + "'")
                .add("path='" + path + "'")
                .add("query=" + queryParams)
                .add("headers=" + headers)
                .add("body.length=" + body.length)
                .toString();
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public List<String> getQueryParam(String name) {
        return queryParams.getOrDefault(name, Collections.emptyList());
    }

    public String getMethod() {
        return method;
    }

    public byte[] getBody() {
        return body;
    }
}
