package ru.netology.http;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class StatusHandler implements Handler {
    private final HttpStatus httpStatus;

    public StatusHandler(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        responseStream.write((
                "HTTP/1.1 " + httpStatus.code + " " + httpStatus.message + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }
}
