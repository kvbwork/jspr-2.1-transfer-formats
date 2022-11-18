package ru.netology.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements Handler {
    private final String filesDirectory;

    public StaticFileHandler(String filesDirectory) {
        this.filesDirectory = filesDirectory;
    }

    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        final var filePath = Path.of(filesDirectory, request.getPath());
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);

        ResponseInfo responseInfo = new ResponseInfo(HttpStatus.OK);
        responseInfo.setContentInfo(mimeType, length);
        responseStream.write(responseInfo.build().getBytes());
        Files.copy(filePath, responseStream);
    }
}
