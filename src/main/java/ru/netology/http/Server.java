package ru.netology.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.netology.http.HttpStatus.*;

public class Server implements Handler {
    private static final int NUM_THREADS = 64;

    private final Map<String, Map<String, Handler>> handlersMethodMap;

    protected final ExecutorService executor;

    public Server() {
        this.handlersMethodMap = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(NUM_THREADS);
    }

    public void addHandler(String method, String path, Handler handler) {
        var methodKey = method.toUpperCase();
        var pathMap = handlersMethodMap.computeIfAbsent(methodKey, k -> new ConcurrentHashMap<>());
        pathMap.put(path, handler);
    }

    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        if (request == null) {
            sendResponse(responseStream, BAD_REQUEST);
            return;
        }

        final var pathMap = handlersMethodMap.get(request.getMethod());
        if (pathMap == null) {
            sendResponse(responseStream, METHOD_NOT_ALLOWED);
            return;
        }

        final var handler = pathMap.get(request.getPath());
        if (handler == null) {
            sendResponse(responseStream, NOT_FOUND);
            return;
        }

        handler.handle(request, responseStream);
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

    protected void sendResponse(OutputStream responseStream, HttpStatus httpStatus) throws IOException {
        responseStream.write(new ResponseInfo(httpStatus).build().getBytes());
        responseStream.flush();
    }
}
