package ru.netology.http;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class RequestReader {
    private final int EXPECTED_REQUEST_LINE_PARTS = 3;
    private final int EXPECTED_HEADER_LINE_PARTS = 2;

    public Request read(BufferedReader in) throws IOException {
        var line = in.readLine();
        var requestParts = line.split(" ");
        if (requestParts.length != EXPECTED_REQUEST_LINE_PARTS) return null;

        var method = requestParts[0];
        var pathParts = requestParts[1].split("\\?|\\#");
        var protocol = requestParts[2];

        var path = pathParts[0];
        var query = pathParts.length > 1 ? pathParts[1] : "";

        Map<String, String> headers = new HashMap<>();
        while (!(line = in.readLine()).isBlank()) {
            var headerParts = line.split(":");
            if (headerParts.length != EXPECTED_HEADER_LINE_PARTS) continue;
            headers.put(headerParts[0].trim(), headerParts[1].trim());
        }

        var body = new StringJoiner("\n");
        while (in.ready() && (line = in.readLine()) != null) {
            body.add(line);
        }

        return new Request(method, path, query, protocol, headers, body.toString());
    }

    public Request read(InputStream in) throws IOException {
        try (var bufReader = new BufferedReader(new InputStreamReader(in))) {
            return read(bufReader);
        }
    }

    public Request read(byte[] content) {
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        try {
            return read(in);
        } catch (IOException ignore) {
        }
        return null;
    }

}
