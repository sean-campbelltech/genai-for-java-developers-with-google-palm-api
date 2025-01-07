package com.campbelltech.genaibot.mapper;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.common.reflect.TypeToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.google.gson.Gson;

@Component
public class ContentMapper {

    public List<Content> map(String prompt) throws IOException, URISyntaxException {
        List<Content> contents = new ArrayList<>();
        Map<String, Object>[] examples = getExamples();
        for (Map<String, Object> example : examples) {
            contents.add(
                    ContentMaker
                            .forRole("user")
                            .fromString((String) example.get("input"))
            );
            contents.add(
                    ContentMaker
                            .forRole("model")
                            .fromString((String) example.get("output"))
            );
        }
        contents.add(ContentMaker
                .forRole("user")
                .fromString(prompt));
        return contents;
    }

    private Map<String, Object>[] getExamples() throws IOException, URISyntaxException {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(ClassLoader.getSystemResource("examples.json").toURI().toURL().openStream())
        )) {
            final Gson gson = new Gson();
            return gson.fromJson(reader, TypeToken.of(Map[].class).getType());
        }
    }
}
