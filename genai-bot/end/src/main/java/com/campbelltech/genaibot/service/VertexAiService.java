package com.campbelltech.genaibot.service;

import com.campbelltech.genaibot.mapper.InstanceMapper;
import com.campbelltech.genaibot.mapper.ParameterMapper;
import com.campbelltech.genaibot.model.ChatHistory;
import com.campbelltech.genaibot.model.ChatMessage;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VertexAiService implements GenAiService {
    
    @Value("${palm.api.project-id}")
    private String projectId;
    @Value("${palm.api.model-id}")
    private String modelId;
    @Value("${palm.api.location}")
    private String location;
    @Value("${palm.api.publisher}")
    private String publisher;
    @Value("${palm.credentials.service-account-file-path}")
    private String serviceAccountFilePath;
    @Value("${palm.credentials.scopes}")
    private String scopes;
    @Value("${palm.bot.name}")
    private String botName;
    @Value("${palm.bot.slogan}")
    private String slogan;

    private final InstanceMapper instanceMapper;
    private final ParameterMapper paramMapper;

    private final List<ChatHistory> history = new ArrayList<>();

    @Autowired
    public VertexAiService(InstanceMapper instanceMapper, ParameterMapper paramMapper) {
        this.instanceMapper = instanceMapper;
        this.paramMapper = paramMapper;
    }

    @Override
    public ChatMessage predict(String prompt) throws IOException, URISyntaxException {
        try (PredictionServiceClient client = PredictionServiceClient.create(getPredictionServiceSettings())) {
            final EndpointName endpointName =  EndpointName.ofProjectLocationPublisherModelName(
                    projectId, location, publisher, modelId);
            PredictResponse predictResponse = client.predict(
                    endpointName, instanceMapper.map(prompt), paramMapper.map());
            return buildChatMessage(prompt, predictResponse);
        }
    }

    private ChatMessage buildChatMessage(String prompt,
                                         PredictResponse predictResponse) {
        String botMessage = "";
        if (predictResponse != null && predictResponse.getPredictionsCount() > 0) {
            botMessage = predictResponse.getPredictions(0)
                    .getStructValue()
                    .getFieldsMap().get("candidates")
                    .getListValue().getValues(0)
                    .getStructValue()
                    .getFieldsMap().get("content")
                    .getStringValue();
        } else {
            botMessage = "The LLM did not provide a response";
        }
        history.add(new ChatHistory("user", prompt));
        history.add(new ChatHistory("bot", botMessage));
        return new ChatMessage(botName, slogan, history);
    }

    private PredictionServiceSettings getPredictionServiceSettings() throws IOException {
        return PredictionServiceSettings.newBuilder()
                .setEndpoint(String.format("%s-aiplatform.googleapis.com:443", location))
                .setCredentialsProvider(FixedCredentialsProvider.create(getGoogleCredentials()))
                .build();
    }

    private GoogleCredentials getGoogleCredentials() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                ClassLoader.getSystemResource(serviceAccountFilePath).openStream())
                .createScoped(Lists.newArrayList(scopes));
        credentials.refreshIfExpired();
        return credentials;
    }
}
