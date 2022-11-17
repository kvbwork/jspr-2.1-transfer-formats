package ru.netology.http;

public class Request {
    private final String protocol;
    private final String path;
    private final String method;

    public Request(String method, String path, String protocol) {
        this.method = method.toUpperCase();
        this.path = path;
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }
}
