package ru.netology.http;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

class RequestReaderTest {
    static final String TEST_BODY = "Строка 1\rСтрока 2\nСтрока 3\nСтрока 4";
    static final byte[] TEST_BODY_BYTES = TEST_BODY.getBytes();

    @Test
    void read_query_params_success() {
        String requestText = "GET /?name=%D0%98%D0%BC%D1%8F" +
                "&value=%D0%97%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%201" +
                "&value=%D0%97%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%202" +
                "#anchor" +
                " HTTP1.1\r\n" +
                "\r\n";
        Request request = RequestReader.read(requestText.getBytes());

        assertThat(request.getQueryParam("name"), hasItems("Имя"));
        assertThat(request.getQueryParam("value"), hasItems("Значение 1", "Значение 2"));
    }

    @Test
    void read_body_post_method_success() {
        String requestText = "POST / HTTP1.1\r\n" +
                "Content-Length: " + TEST_BODY_BYTES.length + "\r\n" +
                "\r\n" +
                TEST_BODY;

        Request request = RequestReader.read(requestText.getBytes());

        assertThat(Arrays.equals(request.getBody(), TEST_BODY_BYTES), is(true));
    }

    @Test
    void skip_body_get_method_success() {
        String requestText = "GET / HTTP1.1\r\n" +
                "Content-Length: " + TEST_BODY_BYTES.length + "\r\n" +
                "\r\n" +
                TEST_BODY;

        Request request = RequestReader.read(requestText.getBytes());

        assertThat(request.getBody().length, is(0));
    }
}