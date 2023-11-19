package com.campbelltech;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            String accessToken = getAccessToken();
            try (HttpClient client = HttpClient.newBuilder().build()) {
                String jsonRequestBody = String.format("""
                        {
                            "instances": [ { "content": "%s" } ],
                            "parameters": %s
                        }
                        """, getContentValue(), getParameters());
                HttpRequest request = buildHttpRequest(accessToken, jsonRequestBody);
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response ->\n" + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        main(args);
    }

    private static HttpRequest buildHttpRequest(String accessToken, String jsonRequestBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(String.format(
                        "https://%s/v1/projects/%s/locations/%s/publishers/%s/models/%s:predict",
                        System.getenv("API_ENDPOINT"),
                        System.getenv("PROJECT_ID"),
                        System.getenv("LOCATION_ID"),
                        System.getenv("PUBLISHER"),
                        System.getenv("MODEL_ID")
                )))
                .header("Content-Type", "application/json")
                .header("Authorization", String.format("Bearer %s", accessToken))
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                .build();
    }

    public static String getContentValue() throws IOException {
        String context = System.getenv("CONTEXT");
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("examples.json")) {
            if (inputStream == null) {
                throw new IOException("Resource not found: examples.json");
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                Gson gson = new Gson();
                Map<String, Object>[] examples = gson.fromJson(reader, TypeToken.of(Map[].class).getType());
                StringBuilder inputOutputInstances = new StringBuilder();
                for (Map<String, Object> example : examples) {
                    String inputValue = (String) example.get("input");
                    String outputValue = (String) example.get("output");
                    inputOutputInstances.append(String.format("input: %s\noutput: %s\n\n", inputValue, outputValue));
                }
                return String.format("%s\n\n%sinput: %s\noutput:\n", context, inputOutputInstances, getPrompt());
            }
        }
    }

    private static String getParameters() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("candidateCount", Integer.parseInt(System.getenv("CANDIDATE_COUNT")));
        parameterMap.put("maxOutputTokens", Integer.parseInt(System.getenv("MAX_OUTPUT_TOKENS")));
        parameterMap.put("temperature", Double.parseDouble(System.getenv("TEMPERATURE")));
        parameterMap.put("topP", Double.parseDouble(System.getenv("TOP-P")));
        parameterMap.put("topK", Integer.parseInt(System.getenv("TOP-K")));
        return new Gson().toJson(parameterMap);
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