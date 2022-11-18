package ru.netology.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static java.util.Optional.ofNullable;

public class Request {
    private final String protocol;
    private final String path;
    private final String query;
    private final String method;
    private final Map<String, String> headers;
    private final String body;

    public Request(String method, String path, String protocol) {
        this(method, path, null, protocol, null, null);
    }

    public Request(
            String method,
            String path,
            String query,
            String protocol,
            Map<String, String> headers,
            String body
    ) {
        this.method = method.toUpperCase();
        this.path = path;
        this.query = ofNullable(query).orElse("");
        this.protocol = protocol;
        this.body = ofNullable(body).orElse("");
        this.headers = ofNullable(headers).orElseGet(HashMap::new);
    }

    public Optional<String> getHeader(String name) {
        return ofNullable(headers.get(name));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Request.class.getSimpleName() + "[", "]")
                .add("protocol='" + protocol + "'")
                .add("path='" + path + "'")
                .add("query='" + query + "'")
                .add("method='" + method + "'")
                .add("headers=" + headers)
                .add("body.length=" + body.length())
                .toString();
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }
}
