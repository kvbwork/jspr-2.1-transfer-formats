package ru.netology.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.netology.http.HttpStatus.OK;

class ServerIntegrationTest {

    static final int TEST_PORT = 31999;
    static final String TEST_PATH = "/test";
    static final String TEST_QUERY = "name=%D0%98%D0%BC%D1%8F" +                // name=Имя
            "&value=%D0%97%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%201" +     // value=Значение 1
            "&value=%D0%97%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%202" +     // value=Значение 2
            "#anchor";
    static final URI TEST_URI = URI.create("http://localhost:" + TEST_PORT + TEST_PATH + "?" + TEST_QUERY);

    static final String TEST_BODY = "Строка 1\rСтрока 2\nСтрока 3\nСтрока 4";
    static final byte[] TEST_BODY_BYTES = TEST_BODY.getBytes();

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
    void connection_get_query_params_success() throws IOException, InterruptedException {
        httpClient.send(
                HttpRequest.newBuilder().GET().uri(TEST_URI).build(),
                HttpResponse.BodyHandlers.discarding()
        );

        assertThat(capturedRequest.getMethod(), equalTo("GET"));
        assertThat(capturedRequest.getPath(), equalTo(TEST_PATH));
        assertThat(capturedRequest.getQueryParam("name"), hasItems("Имя"));
        assertThat(capturedRequest.getQueryParam("value"), hasItems("Значение 1", "Значение 2"));

    }

    @Test
    void connection_post_body_success() throws IOException, InterruptedException {
        httpClient.send(
                HttpRequest.newBuilder().uri(TEST_URI)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(TEST_BODY_BYTES)).build(),
                HttpResponse.BodyHandlers.discarding()
        );

        assertThat(capturedRequest.getMethod(), equalTo("POST"));
        assertThat(capturedRequest.getPath(), equalTo(TEST_PATH));
        assertThat(Arrays.equals(capturedRequest.getBody(), TEST_BODY_BYTES), is(true));
    }

}