package ru.netology.http.codec;

import java.util.Optional;
import java.util.StringJoiner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

public class ContentPart {
    private final String fieldName;
    private final Optional<String> fileName;
    private final Optional<String> contentType;
    private final Optional<byte[]> content;
    private String cachedContentString = null;

    public ContentPart(String fieldName, String fileName, String contentType, byte[] content) {
        this.fieldName = fieldName;
        this.fileName = ofNullable(fileName);
        this.contentType = ofNullable(contentType);
        this.content = ofNullable(content);
    }

    @Override
    public String toString() {
        final var joiner = new StringJoiner(", ", ContentPart.class.getSimpleName() + "[", "]")
                .add("fieldName='" + getFieldName() + "'");
        if (isFile()) {
            getFileName().ifPresent(v -> joiner.add("fileName=" + v));
            getContentType().ifPresent(v -> joiner.add("contentType=" + v));
            joiner.add("content.length=" + getContentLength());
        } else {
            getContentAsString().ifPresent(v -> joiner.add("value=" + v));
        }
        return joiner.toString();
    }

    public boolean isFile() {
        return getFileName().isPresent();
    }

    public String getFieldName() {
        return fieldName;
    }

    public Optional<String> getFileName() {
        return fileName;
    }

    public Optional<String> getContentType() {
        return contentType;
    }

    public Optional<byte[]> getContent() {
        return content;
    }

    public Optional<String> getContentAsString() {
        return getContent()
                .map(contentBytes -> {
                    if (cachedContentString == null)
                        cachedContentString = new String(contentBytes, UTF_8);
                    return cachedContentString;
                });
    }

    public int getContentLength() {
        return getContent()
                .map(content -> content.length)
                .orElse(0);
    }
}
