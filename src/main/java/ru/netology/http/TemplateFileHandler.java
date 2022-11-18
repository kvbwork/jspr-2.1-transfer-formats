package ru.netology.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class TemplateFileHandler implements Handler {
    private final String filesDirectory;

    public TemplateFileHandler(String filesDirectory) {
        this.filesDirectory = filesDirectory;
    }

    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        final var filePath = Path.of(filesDirectory, request.getPath());
        final var mimeType = Files.probeContentType(filePath);

        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();

        ResponseInfo responseInfo = new ResponseInfo(HttpStatus.OK);
        responseInfo.setContentInfo(mimeType, content.length);
        responseStream.write(responseInfo.build().getBytes());
        responseStream.write(content);
    }
}
