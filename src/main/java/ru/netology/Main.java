package ru.netology;

import ru.netology.http.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        final var validPaths = List.of(
                "/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
                "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html",
                "/events.js"
        );
        int port = 9999;
        var filesDir = "./public";
        var staticFileHandler = new StaticFileHandler(filesDir);
        var templateFileHandler = new TemplateFileHandler(filesDir);

        Server server = new Server();

        validPaths.forEach(path -> server.addHandler("GET", path, staticFileHandler));
        server.addHandler("GET", "/classic.html", templateFileHandler);

        // добавление handler'ов (обработчиков)
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                byte[] content = "Ответ на запрос /messages".getBytes();
                String mimeType = "text/plain;charset=UTF-8";
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.write(content);
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                responseStream.write((
                        "HTTP/1.1 405 Method Not Allowed\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
            }
        });
        System.out.println("Running server on port: " + port);
        server.listen(port);

    }
}


