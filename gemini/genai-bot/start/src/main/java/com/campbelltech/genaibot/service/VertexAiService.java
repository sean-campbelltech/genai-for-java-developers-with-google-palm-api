package com.campbelltech.genaibot.service;

import com.campbelltech.genaibot.mapper.ContentMapper;
import com.campbelltech.genaibot.mapper.GenerationConfigMapper;
import com.campbelltech.genaibot.mapper.SafetySettingsMapper;
import com.campbelltech.genaibot.model.ChatMessage;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
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
    
    @Value("${gemini.api.project-id}")
    private String projectId;
    @Value("${gemini.api.model-id}")
    private String modelId;
    @Value("${gemini.api.location}")
    private String location;
    @Value("${gemini.credentials.service-account-file-path}")
    private String serviceAccountFilePath;
    @Value("${gemini.credentials.scopes}")
    private String scopes;
    @Value("${gemini.bot.name}")
    private String botName;
    @Value("${gemini.bot.slogan}")
    private String slogan;
    @Value("${gemini.bot.context}")
    private String context;

    private final ContentMapper contentMapper;
    private final GenerationConfigMapper generationConfigMapper;
    private final SafetySettingsMapper safetySettingsMapper;

    @Autowired
    public VertexAiService(
            ContentMapper contentMapper,
            GenerationConfigMapper generationConfigMapper,
            SafetySettingsMapper safetySettingsMapper
            ) {
        this.contentMapper = contentMapper;
        this.generationConfigMapper = generationConfigMapper;
        this.safetySettingsMapper = safetySettingsMapper;
    }

    @Override
    public ChatMessage generateContent(String prompt) throws IOException, URISyntaxException {
        try (VertexAI vertexAi = new VertexAI.Builder()
                .setLocation(location)
                .setProjectId(projectId)
                .setCredentials(getCredentials())
                .build()) {
            GenerativeModel model = new GenerativeModel.Builder()
                    .setModelName(modelId) // e.g. gemini-1.5-flash-002
                    .setVertexAi(vertexAi)
                    .setSystemInstruction(ContentMaker.fromString(context))
                    .setGenerationConfig(generationConfigMapper.map())
                    .setSafetySettings(safetySettingsMapper.map())
                    .build();
            GenerateContentResponse response = model.generateContent(contentMapper.map(prompt));
            return buildChatMessage(prompt, response);
        }
    }

    private ChatMessage buildChatMessage(String prompt, GenerateContentResponse response) {
        // TODO: Complete Implementation of buildChatMessage method
        return new ChatMessage();
    }

    private GoogleCredentials getCredentials() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                ClassLoader.getSystemResource(serviceAccountFilePath).openStream())
                .createScoped(Lists.newArrayList(scopes));
        credentials.refreshIfExpired();
        return credentials;
    }
}
