package ru.netology.http;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.http.codec.FormUrlEncodedDecoder;
import ru.netology.http.codec.MultipartFormDataDecoder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.hc.core5.http.ContentType.DEFAULT_TEXT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.netology.http.HttpStatus.OK;

class ServerIntegrationTest {

    static final int TEST_PORT = 31999;
    static final String TEST_PATH = "/test";
    static final String TEST_QUERY = "name=%D0%98%D0%BC%D1%8F" +                // name=Имя
            "&value=%D0%97%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%201" +     // value=Значение 1
            "&value=%D0%97%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%202";      // value=Значение 2
    static final URI TEST_URI = URI.create("http://localhost:" + TEST_PORT + TEST_PATH);
    static final URI TEST_URI_WITH_QUERY = URI.create(TEST_URI.toString() + "?" + TEST_QUERY);

    static final String TEST_BODY = "Строка 1\rСтрока 2\nСтрока 3\nСтрока 4";
    static final byte[] TEST_BODY_BYTES = TEST_BODY.getBytes();

    static final String CONTENT_TYPE = "Content-Type";
    static final String FORM_URLENCODED_TYPE = "application/x-www-form-urlencoded";

    static HttpClient httpClient;
    static Server server;
    static Thread serverThread;
    static Request capturedRequest;

    @BeforeAll
    static void beforeAll() {
        httpClient = HttpClient.newHttpClient();

        server = new Server();
        server.addHandler("GET", TEST_PATH, (request, responseStream) -> {
            capturedRequest = request;
            server.sendResponse(responseStream, OK);
        });

        server.addHandler("POST", TEST_PATH, (request, responseStream) -> {
            capturedRequest = request;
            server.sendResponse(responseStream, OK);
        });

        serverThread = new Thread(() -> server.listen(TEST_PORT));
        serverThread.start();
    }

    @AfterAll
    static void afterAll() {
        httpClient = null;
        serverThread.interrupt();
        serverThread = null;
        server = null;
    }

    @BeforeEach
    void setUp() {
        capturedRequest = null;
    }

    @Test
    void connection_get_success() throws IOException, InterruptedException {
        httpClient.send(
                HttpRequest.newBuilder().GET().uri(TEST_URI).build(),
                HttpResponse.BodyHandlers.discarding()
        );

        assertThat(capturedRequest.getMethod(), equalTo("GET"));
        assertThat(capturedRequest.getPath(), equalTo(TEST_PATH));
    }

    @Test
    void get_query_params_success() throws IOException, InterruptedException {
        httpClient.send(
                HttpRequest.newBuilder().GET().uri(TEST_URI_WITH_QUERY).build(),
                HttpResponse.BodyHandlers.discarding()
        );

        assertThat(capturedRequest.getMethod(), equalTo("GET"));
        assertThat(capturedRequest.getPath(), equalTo(TEST_PATH));
        assertThat(capturedRequest.getQueryParam("name"), hasItems("Имя"));
        assertThat(capturedRequest.getQueryParam("value"), hasItems("Значение 1", "Значение 2"));

    }

    @Test
    void post_body_success() throws IOException, InterruptedException {
        httpClient.send(
                HttpRequest.newBuilder().uri(TEST_URI)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(TEST_BODY_BYTES)).build(),
                HttpResponse.BodyHandlers.discarding()
        );

        assertThat(capturedRequest.getMethod(), equalTo("POST"));
        assertThat(capturedRequest.getPath(), equalTo(TEST_PATH));
        assertThat(Arrays.equals(capturedRequest.getBody(), TEST_BODY_BYTES), is(true));
    }

    @Test
    void post_form_urlencoded_success() throws IOException, InterruptedException {
        httpClient.send(
                HttpRequest.newBuilder().uri(TEST_URI)
                        .header(CONTENT_TYPE, FORM_URLENCODED_TYPE)
                        .POST(HttpRequest.BodyPublishers.ofString(TEST_QUERY)).build(),
                HttpResponse.BodyHandlers.discarding()
        );

        var formData = new FormUrlEncodedDecoder(capturedRequest.getBody());

        assertThat(capturedRequest.getHeader(CONTENT_TYPE).orElseThrow(), equalTo(FORM_URLENCODED_TYPE));
        assertThat(formData.getPostParam("name"), hasItems("Имя"));
        assertThat(formData.getPostParam("value"), hasItems("Значение 1", "Значение 2"));
    }

    @Test
    void post_form_multipart_success() throws IOException {
        var fileFieldName = "file";
        var filename = "testfile.dat";
        var fileCount = 1;

        var valueFieldName = "value";
        var valueList = List.of("Значение1", "Значение2");

        var testTextContentType = DEFAULT_TEXT.withCharset(UTF_8);

        try (var apacheHttpClient = HttpClients.createDefault()) {
            var httpPost = new HttpPost(TEST_URI);
            var entityBuilder = MultipartEntityBuilder.create()
                    .addBinaryBody(fileFieldName, TEST_BODY_BYTES, APPLICATION_OCTET_STREAM, filename);
            valueList.forEach(v -> entityBuilder.addTextBody(valueFieldName, v, testTextContentType));
            httpPost.setEntity(entityBuilder.build());
            apacheHttpClient.execute(httpPost, response -> null);
        }

        var multipartData = new MultipartFormDataDecoder(capturedRequest);
        assertThat(multipartData.getPart(fileFieldName).size(), is(fileCount));
        assertThat(multipartData.getPart(valueFieldName).size(), is(valueList.size()));

        var capturedFilePart = multipartData.getPart(fileFieldName).get(0);
        assertThat(capturedFilePart.isFile(), is(true));
        assertThat(capturedFilePart.getFileName().orElseThrow(), equalTo(filename));
        assertThat(Arrays.equals(capturedFilePart.getContent().orElseThrow(), TEST_BODY_BYTES), is(true));

        var capturedValueList = multipartData.getPostParam(valueFieldName);
        assertThat(capturedValueList.size(), is(valueList.size()));
        assertThat(capturedValueList, containsInAnyOrder(valueList.toArray(String[]::new)));
    }

}