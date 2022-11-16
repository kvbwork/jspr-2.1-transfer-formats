package ru.netology.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int NUM_THREADS = 64;

    private final Set<String> routes = new CopyOnWriteArraySet<>();

    protected final ExecutorService executor;

    public Server() {
        this.executor = Executors.newFixedThreadPool(NUM_THREADS);
    }

    public Set<String> getRoutes() {
        return routes;
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                var socket = serverSocket.accept();
                try {
                    handleConnection(socket);
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void handleConnection(Socket socket) throws IOException {
        var connectionHandler = new ConnectionHandler(socket, this);
        executor.submit(connectionHandler);
    }
}
