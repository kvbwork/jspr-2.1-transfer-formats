package ru.netology;

import ru.netology.http.*;
import ru.netology.http.codec.ContentPart;
import ru.netology.http.codec.FormUrlEncodedDecoder;
import ru.netology.http.codec.MultipartFormDataDecoder;

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

        // пример обработки форм и извлечения параметров
        server.addHandler("POST", "/", (request, responseStream) -> {
            System.out.println(request);

            System.out.println("Query параметры запроса:");
            request.getQueryParams().forEach((name, valuesList) -> {
                System.out.println(name + " = " + valuesList);
            });

            var contentType = request.getContentType().orElse("");

            if (contentType.contains("form-urlencoded")) {
                var urlencodedBody = new FormUrlEncodedDecoder(request.getBody());

                System.out.println("POST параметры запроса:");
                urlencodedBody.getPostParams().forEach((name, valuesList) -> {
                    System.out.println(name + " = " + valuesList);
                });
            } else if (contentType.contains("multipart/")) {
                var multipartBody = new MultipartFormDataDecoder(request);

                System.out.println("Все части запроса:");
                multipartBody.getParts().values().forEach(System.out::println);

                System.out.println("из них POST параметры:");
                multipartBody.getPostParams().forEach((name, valuesList) -> {
                    System.out.println(name + " = " + valuesList);
                });

                System.out.println("файлы:");
                multipartBody.getParts().values().stream()
                        .flatMap(List::stream)
                        .filter(ContentPart::isFile)
                        .forEach(part -> System.out.printf("%s (%d байт)",
                                part.getFileName().orElse("''"), part.getContentLength()));
            }
            responseStream.write(new ResponseInfo(HttpStatus.OK).build().getBytes());
        });
        System.out.println("Running server on port: " + port);
        server.listen(port);

    }
}


