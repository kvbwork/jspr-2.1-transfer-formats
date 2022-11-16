package ru.netology.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class ConnectionHandler implements Runnable {

    private final String staticFilesPath = "./public";
    private final String templatesPath = "./public";

    protected final Socket socket;
    protected final Server server;
    protected final BufferedReader in;
    protected final BufferedOutputStream out;

    public ConnectionHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            final var path = extractPath();
            if (!validatePath(path)) return;
            if (processDynamicContent(path)) return;
            processStaticContent(path);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    protected boolean processStaticContent(String path) throws IOException {
        final var filePath = Path.of(staticFilesPath, path);
        final var mimeType = Files.probeContentType(filePath);

        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
        return true;
    }

    protected boolean processDynamicContent(String path) throws IOException {
        final var filePath = Path.of(templatesPath, path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return true;
        }
        return false;
    }

    protected boolean validatePath(String path) throws IOException {
        if (server.getRoutes().contains(path)) return true;
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
        return false;
    }

    protected String extractPath() throws IOException {
        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            return null;
        }

        final var path = parts[1];
        return path;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
