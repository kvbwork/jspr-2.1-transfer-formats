package ru.netology.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    protected final Socket socket;
    protected final Handler rootHandler;
    protected final BufferedReader in;
    protected final BufferedOutputStream out;

    public ConnectionHandler(Socket socket, Handler rootHandler) throws IOException {
        this.socket = socket;
        this.rootHandler = rootHandler;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            final var request = parseRequest(in);
            if (request != null) {
                rootHandler.handle(request, out);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    protected Request parseRequest(BufferedReader in) throws IOException {
        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            return null;
        }

        return new Request(parts[0], parts[1], parts[2]);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
