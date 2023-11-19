package com.campbelltech;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // NB! In JDK 21 the HttpClient now also implements AutoCloseable and can now be used in try-with-resources block
        try (HttpClient client = HttpClient.newBuilder().build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(
                            "https://generativelanguage.googleapis.com/v1beta3/models/%s:generateMessage?key=%s",
                            System.getenv("MODEL_ID"), System.getenv("API_KEY")
                    )))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(getJsonRequestBody()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response ->\n" + response.body());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        // recursively call main to ask for user prompt again
        main(args);
    }

    private static String getJsonRequestBody() throws IOException, URISyntaxException {
        // US locale ensures that doubles contains dots (.) and not commas (,)
        Locale.setDefault(Locale.US);
        return String.format("""
                {
                    "prompt": {
                        "context": "%s",
                        "examples": %s,
                        "messages": [{ "content": "%s"} ],
                    },
                    "temperature": %.1f,
                    "top_k": %d,
                    "top_p": %.1f,
                    "candidate_count": %d
                }
                """,
                System.getenv("CONTEXT"), getExamples(), getPrompt(),
                Double.parseDouble(System.getenv("TEMPERATURE")),
                Integer.parseInt(System.getenv("TOP-K")),
                Double.parseDouble(System.getenv("TOP-P")),
                Integer.parseInt(System.getenv("CANDIDATE_COUNT")));
    }

    private static String getPrompt() {
        System.out.println("Type a prompt:");
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    public static String getExamples() throws IOException, URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("examples.json").toURI());
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}