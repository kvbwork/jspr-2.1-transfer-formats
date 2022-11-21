package ru.netology.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements Runnable {

    protected final Socket socket;
    protected final Handler rootHandler;
    protected final BufferedInputStream in;
    protected final BufferedOutputStream out;

    public ConnectionHandler(Socket socket, Handler rootHandler) throws IOException {
        this.socket = socket;
        this.rootHandler = rootHandler;
        this.in = new BufferedInputStream(socket.getInputStream());
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

    protected Request parseRequest(BufferedInputStream in) throws IOException {
        return new RequestReader(in).read();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
