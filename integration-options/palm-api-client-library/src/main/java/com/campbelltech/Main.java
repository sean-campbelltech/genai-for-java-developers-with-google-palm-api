package com.campbelltech;

import com.google.ai.generativelanguage.v1beta3.*;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        try (DiscussServiceClient client = initClient()) {
            MessagePrompt messagePrompt = buildMessagePrompt();
            GenerateMessageRequest request = GenerateMessageRequest.newBuilder()
                    .setModel(System.getenv("MODEL_ID"))
                    .setPrompt(messagePrompt)
                    .setTemperature(Float.parseFloat(System.getenv("TEMPERATURE")))
                    .setCandidateCount(Integer.parseInt(System.getenv("CANDIDATE_COUNT")))
                    .setTopK(Integer.parseInt(System.getenv("TOP-K")))
                    .setTopP(Float.parseFloat(System.getenv("TOP-P")))
                    .build();
            GenerateMessageResponse response = client.generateMessage(request);
            System.out.println(response);
        }
        main(args);
    }

    private static String getPrompt() {
        System.out.println("Type a prompt:");
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    private static DiscussServiceClient initClient() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-goog-api-key", System.getenv("API_KEY"));
        DiscussServiceSettings settings = DiscussServiceSettings.newBuilder()
                .setTransportChannelProvider(InstantiatingGrpcChannelProvider.newBuilder()
                        .setHeaderProvider(FixedHeaderProvider.create(headers))
                        .build())
                .setCredentialsProvider(FixedCredentialsProvider.create(null))
                .build();
        return DiscussServiceClient.create(settings);
    }

    private static MessagePrompt buildMessagePrompt() throws IOException {
        return MessagePrompt.newBuilder()
                .addMessages(Message.newBuilder()
                        .setAuthor("User")
                        .setContent(getPrompt())
                        .build())
                .setContext(System.getenv("CONTEXT"))
                .addAllExamples(buildExamples())
                .build();
    }

    private static List<Example> buildExamples() throws IOException {
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("examples.json");
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            Gson gson = new Gson();
            Map<String, Object>[] jsonExamples = gson.fromJson(reader, TypeToken.of(Map[].class).getType());
            List<Example> examples = new ArrayList<>();
            for (Map<String, Object> jsonExample : jsonExamples) {
                String inputValue = (String) jsonExample.get("input");
                String outputValue = (String) jsonExample.get("output");
                examples.add(Example.newBuilder()
                        .setInput(Message.newBuilder()
                                .setContent(inputValue)
                                .build())
                        .setOutput(Message.newBuilder()
                                .setContent(outputValue)
                                .build())
                        .build());
            }
            return examples;
        }
    }
}