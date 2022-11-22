package ru.netology.http.codec;

import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import ru.netology.http.Request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;

public class MultipartFormDataDecoder {

    private final Request request;
    private final RequestContext requestContext;
    private final Map<String, List<ContentPart>> contentPartsMap;

    public MultipartFormDataDecoder(Request request) throws IOException {
        this.request = request;
        this.requestContext = new RequestContext() {
            @Override
            public String getCharacterEncoding() {
                return "";
            }

            @Override
            public String getContentType() {
                return request.getContentType().orElse("");
            }

            @Override
            public int getContentLength() {
                return request.getContentLength();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(request.getBody());
            }
        };
        this.contentPartsMap = new HashMap<>();
        parse();
    }

    protected void parse() throws IOException {
        var upload = new FileUpload();

        try {
            var iter = upload.getItemIterator(requestContext);
            while (iter.hasNext()) {
                var item = iter.next();
                try (final var contentBuf = new ByteArrayOutputStream()) {
                    item.openStream().transferTo(contentBuf);

                    var contentPart = new ContentPart(
                            item.getFieldName(),
                            item.getName(),
                            item.getContentType(),
                            contentBuf.toByteArray()
                    );
                    storeContentPart(contentPart);
                }
            }
        } catch (FileUploadException ex) {
            ex.printStackTrace();
        }
    }

    protected void storeContentPart(ContentPart contentPart) {
        contentPartsMap.computeIfAbsent(contentPart.getFieldName(), k -> new ArrayList<>())
                .add(contentPart);
    }

    public List<ContentPart> getPart(String fieldName) {
        return contentPartsMap.getOrDefault(fieldName, emptyList());
    }

    public Map<String, List<ContentPart>> getParts() {
        return contentPartsMap;
    }

    public Map<String, List<String>> getPostParams() {
        return getParts().entrySet().stream()
                .map(e -> Map.entry(
                        e.getKey(),
                        e.getValue().stream()
                                .filter(not(ContentPart::isFile))
                                .map(ContentPart::getContentAsString)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())))
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<String> getPostParam(String name) {
        return getPart(name)
                .stream()
                .filter(not(ContentPart::isFile))
                .map(ContentPart::getContentAsString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}
