package com.campbelltech;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        try {
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpRequest request = buildHttpRequest();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response ->\n" + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        main(args);
    }

    private static HttpRequest buildHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format(
                        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                        System.getenv("MODEL_ID"),  // e.g. gemini-1.5-flash-002
                        System.getenv("API_KEY")
                )))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody().toString()))
                .build();
    }

    private static JsonArray buildContent() {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("contents.json")) {
            if (inputStream == null) {
                throw new IOException("Resource not found: contents.json");
            }
            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            jsonContent = jsonContent
                    .replace("{CONTEXT}", System.getenv("CONTEXT"))
                    .replace("{USER_PROMPT}", getPrompt());
            Gson gson = new Gson();
            return gson.fromJson(jsonContent, JsonArray.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonArray();
        }
    }

    private static JsonObject buildRequestBody() {
        JsonObject jsonRequestBody = new JsonObject();
        jsonRequestBody.add("contents", buildContent());
        jsonRequestBody.add("generationConfig", buildGenerationConfig());
        jsonRequestBody.add("safetySettings", buildSafetySettings());
        return jsonRequestBody;
    }

    private static JsonObject buildGenerationConfig() {
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", Double.parseDouble(System.getenv("TEMPERATURE")));
        generationConfig.addProperty("maxOutputTokens", Integer.parseInt(System.getenv("MAX_OUTPUT_TOKENS")));
        generationConfig.addProperty("topP", Double.parseDouble(System.getenv("TOP-P")));
        generationConfig.addProperty("topK", Integer.parseInt(System.getenv("TOP-K")));
        return generationConfig;
    }

    private static JsonObject createSafetySetting(String category) {
        JsonObject safetySetting = new JsonObject();
        safetySetting.addProperty("category", category);
        safetySetting.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        return safetySetting;
    }

    private static JsonArray buildSafetySettings() {
        JsonArray safetySettings = new JsonArray();
        safetySettings.add(createSafetySetting("HARM_CATEGORY_HATE_SPEECH"));
        safetySettings.add(createSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT"));
        safetySettings.add(createSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT"));
        safetySettings.add(createSafetySetting("HARM_CATEGORY_HARASSMENT"));
        safetySettings.add(createSafetySetting("HARM_CATEGORY_CIVIC_INTEGRITY"));
        return safetySettings;
    }

    private static String getPrompt() {
        System.out.println("Type a prompt:");
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }
}