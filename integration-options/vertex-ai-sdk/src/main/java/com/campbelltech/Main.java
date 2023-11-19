package com.campbelltech;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    private final static String LOCATION_ID = System.getenv("LOCATION_ID");

    public static void main(String[] args) throws IOException {
        try (PredictionServiceClient predictionServiceClient = PredictionServiceClient.create(getPredictionServiceSettings())) {
            final EndpointName endpointName =  EndpointName.ofProjectLocationPublisherModelName(
                    System.getenv("PROJECT_ID"), LOCATION_ID,
                    System.getenv("PUBLISHER"),
                    System.getenv("MODEL_ID"));
            PredictResponse predictResponse = predictionServiceClient.predict(
                    endpointName, getInstances(), getParameterValue());
            System.out.println("Response ->\n" + predictResponse);
        }
        main(args);
    }

    private static String getPrompt() {
        System.out.println("Type a prompt:");
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    private static PredictionServiceSettings getPredictionServiceSettings() throws IOException {
        String endpoint = String.format("%s-aiplatform.googleapis.com:443", LOCATION_ID);
        return PredictionServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .setCredentialsProvider(FixedCredentialsProvider.create(getGoogleCredentials()))
                .build();
    }

    private static GoogleCredentials getGoogleCredentials() throws IOException {
        return GoogleCredentials
                .fromStream(Main.class.getClassLoader().getResourceAsStream(
                        System.getenv("SERVICE_ACCOUNT_FILE_PATH"))
                )
                .createScoped(Lists.newArrayList(System.getenv("SCOPES")));
    }

    public static String getContentValue() throws IOException {
        String context = System.getenv("CONTEXT");
        try (InputStreamReader reader = new InputStreamReader(
                     Main.class.getClassLoader().getResourceAsStream("examples.json"))) {
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

    private static List<Value> getInstances() throws IOException {
        Value.Builder instanceValue = Value.newBuilder();
        String instance = String.format("{ \"content\": \"%s\" }", getContentValue());
        JsonFormat.parser().merge(instance, instanceValue);
        List<Value> instances = new ArrayList<>();
        instances.add(instanceValue.build());
        return instances;
    }

    private static Value getParameterValue() throws InvalidProtocolBufferException {
        Value.Builder parameterValueBuilder = Value.newBuilder();
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("candidateCount", Integer.parseInt(System.getenv("CANDIDATE_COUNT")));
        parameterMap.put("maxOutputTokens", Integer.parseInt(System.getenv("MAX_OUTPUT_TOKENS")));
        parameterMap.put("temperature", Double.parseDouble(System.getenv("TEMPERATURE")));
        parameterMap.put("topP", Double.parseDouble(System.getenv("TOP-P")));
        parameterMap.put("topK", Integer.parseInt(System.getenv("TOP-K")));
        JsonFormat.parser().merge(new Gson().toJson(parameterMap), parameterValueBuilder);
        return parameterValueBuilder.build();
    }
}