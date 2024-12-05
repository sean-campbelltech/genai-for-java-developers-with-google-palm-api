package com.campbelltech;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
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
            String accessToken = getAccessToken();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                HttpRequest request = buildHttpRequest(accessToken);
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response ->\n" + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        main(args);
    }

    private static HttpRequest buildHttpRequest(String accessToken) {
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format(
                        "https://%s/v1/projects/%s/locations/%s/publishers/%s/models/%s:streamGenerateContent",
                        System.getenv("API_ENDPOINT"),
                        System.getenv("PROJECT_ID"),
                        System.getenv("LOCATION_ID"),
                        System.getenv("PUBLISHER"),
                        System.getenv("MODEL_ID") // e.g. gemini-1.5-flash-002
                )))
                .header("Content-Type", "application/json")
                .header("Authorization", String.format("Bearer %s", accessToken))
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody().toString()))
                .build();
    }

    private static JsonObject buildRequestBody() {
        JsonObject jsonRequestBody = new JsonObject();
        jsonRequestBody.add("contents", buildContent());
        jsonRequestBody.add("systemInstruction", buildSystemInstructions());
        jsonRequestBody.add("generationConfig", buildGenerationConfig());
        jsonRequestBody.add("safetySettings", buildSafetySettings());
        return jsonRequestBody;
    }

    private static JsonArray buildContent() {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("contents.json")) {
            if (inputStream == null) {
                throw new IOException("Resource not found: contents.json");
            }
            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            jsonContent = jsonContent.replace("{USER_PROMPT}", getPrompt());
            Gson gson = new Gson();
            return gson.fromJson(jsonContent, JsonArray.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonArray();
        }
    }

    private static JsonObject buildSystemInstructions() {
        JsonObject systemInstruction = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", System.getenv("CONTEXT"));
        parts.add(part);
        systemInstruction.add("parts", parts);
        return systemInstruction;
    }

    private static JsonObject buildGenerationConfig() {
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("candidateCount", Integer.parseInt(System.getenv("CANDIDATE_COUNT")));
        generationConfig.addProperty("temperature", Double.parseDouble(System.getenv("TEMPERATURE")));
        generationConfig.addProperty("maxOutputTokens", Integer.parseInt(System.getenv("MAX_OUTPUT_TOKENS")));
        generationConfig.addProperty("topP", Double.parseDouble(System.getenv("TOP-P")));
        generationConfig.addProperty("seed", Integer.parseInt(System.getenv("SEED")));
        return generationConfig;
    }

    private static JsonArray buildSafetySettings() {
        JsonArray safetySettings = new JsonArray();
        safetySettings.add(createSafetySetting("HARM_CATEGORY_HATE_SPEECH"));
        safetySettings.add(createSafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT"));
        safetySettings.add(createSafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT"));
        safetySettings.add(createSafetySetting("HARM_CATEGORY_HARASSMENT"));
        return safetySettings;
    }

    private static JsonObject createSafetySetting(String category) {
        JsonObject safetySetting = new JsonObject();
        safetySetting.addProperty("category", category);
        safetySetting.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        return safetySetting;
    }

    private static String getPrompt() {
        System.out.println("Type a prompt:");
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    private static String getAccessToken() throws IOException {
        try (InputStream serviceAccountStream = Main.class.getClassLoader().getResourceAsStream(
                System.getenv("SERVICE_ACCOUNT_FILE_PATH"))) {
            if (serviceAccountStream == null) {
                throw new FileNotFoundException("Service account file not found");
            }
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(serviceAccountStream)
                    .createScoped(Lists.newArrayList(System.getenv("SCOPES")));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        }
    }
}