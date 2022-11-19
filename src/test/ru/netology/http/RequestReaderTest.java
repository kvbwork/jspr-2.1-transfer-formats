package ru.netology.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

class RequestReaderTest {

    RequestReader sut;

    @BeforeEach
    void setUp() {
        sut = new RequestReader();
    }

    @AfterEach
    void tearDown() {
        sut = null;
    }

    @Test
    void read_query_params_success() {
        String requestText = "GET /?name=%D0%98%D0%BC%D1%8F" +
                "&value=%D0%97%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%201" +
                "&value=%D0%97%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5%202" +
                "#anchor" +
                " HTTP1.1\r\n" +
                "\r\n";
        Request request = sut.read(requestText.getBytes());

        assertThat(request.getQueryParam("name"), hasItems("Имя"));
        assertThat(request.getQueryParam("value"), hasItems("Значение 1", "Значение 2"));
    }
}