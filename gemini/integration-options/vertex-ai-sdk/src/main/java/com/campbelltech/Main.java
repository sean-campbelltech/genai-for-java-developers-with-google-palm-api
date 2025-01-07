package com.campbelltech;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException {
        try (VertexAI vertexAi = new VertexAI.Builder()
                .setLocation(System.getenv("LOCATION"))
                .setProjectId(System.getenv("PROJECT_ID"))
                .setCredentials(getCredentials())
                .build()) {
            GenerativeModel model = new GenerativeModel.Builder()
                    .setModelName(System.getenv("MODEL_ID")) // e.g. gemini-1.5-flash-002
                    .setVertexAi(vertexAi)
                    .setSystemInstruction(ContentMaker.fromString(System.getenv("CONTEXT")))
                    .setGenerationConfig(getGenerationConfig())
                    .setSafetySettings(getSafetySettings())
                    .build();
            GenerateContentResponse response = model.generateContent(buildContents());
            String modelResponse = response.getCandidatesCount() > 0
                    ? response.getCandidates(0).getContent().getParts(0).getText()
                    : "No Response";
            System.out.println("Response ->\n" + modelResponse);
        }
        main(args);
    }

    private static List<Content> buildContents() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("examples.json")))
        ) {
            Gson gson = new Gson();
            Map<String, Object>[] examples = gson.fromJson(reader, TypeToken.of(Map[].class).getType());
            List<Content> contents = new ArrayList<>();
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
            contents.add(addUserPrompt());
            return contents;
        }
    }

    private static String getPrompt() {
        System.out.println("Type a prompt:");
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    private static Content addUserPrompt() {
        return ContentMaker
                .forRole("user")
                .fromString(getPrompt());
    }

    private static GenerationConfig getGenerationConfig() {
        return GenerationConfig.newBuilder()
                .setCandidateCount(Integer.parseInt(System.getenv("CANDIDATE_COUNT")))
                .setTemperature(Float.parseFloat(System.getenv("TEMPERATURE")))
                .setMaxOutputTokens(Integer.parseInt(System.getenv("MAX_OUTPUT_TOKENS")))
                .setTopP(Float.parseFloat(System.getenv("TOP-P")))
                .setSeed(Integer.parseInt(System.getenv("SEED")))
                .build();
    }

    private static List<SafetySetting> getSafetySettings() {
        return Arrays.asList(
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                        .build()
        );
    }

    private static GoogleCredentials getCredentials() throws IOException {
        return GoogleCredentials
                .fromStream(
                        Main.class.getClassLoader().getResourceAsStream(System.getenv("SERVICE_ACCOUNT_FILE_PATH"))
                )
                .createScoped(Lists.newArrayList(System.getenv("SCOPES")));
    }
}