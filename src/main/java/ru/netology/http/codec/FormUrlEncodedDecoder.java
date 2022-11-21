package ru.netology.http.codec;

import org.apache.hc.core5.net.URLEncodedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;

public class FormUrlEncodedDecoder {

    private final Map<String, List<String>> postParamsMap;

    public FormUrlEncodedDecoder(byte[] content) {
        this(new String(content));
    }

    public FormUrlEncodedDecoder(String sourceString) {
        this.postParamsMap = parseUrlEncodedParams(sourceString);
    }

    public static Map<String, List<String>> parseUrlEncodedParams(String sourceString) {
        var pairList = URLEncodedUtils.parse(sourceString, UTF_8);
        var paramsMap = new HashMap<String, List<String>>();

        pairList.forEach(pair ->
                paramsMap.computeIfAbsent(pair.getName(), k -> new ArrayList<>())
                        .add(pair.getValue())
        );

        return paramsMap;
    }

    public List<String> getPostParam(String name) {
        return postParamsMap.getOrDefault(name, emptyList());
    }

    public Map<String, List<String>> getPostParams() {
        return postParamsMap;
    }

}
