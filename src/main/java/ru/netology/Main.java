package ru.netology;

import ru.netology.http.Server;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        final var validPaths = List.of(
                "/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
                "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html",
                "/events.js"
        );
        int port = 9999;

        Server server = new Server();
        server.getRoutes().addAll(validPaths);

        System.out.println("Running server on port: " + port);
        server.listen(port);

    }
}


