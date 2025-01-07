package com.campbelltech.genaibot.mapper;

import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class GenerationConfigMapper {

    @Value("${gemini.api.parameters.candidateCount}")
    private int candidateCount;
    @Value("${gemini.api.parameters.temperature}")
    private float temperature;
    @Value("${gemini.api.parameters.max-output-tokens}")
    private int maxOutputTokens;
    @Value("${gemini.api.parameters.top-p}")
    private float topP;
    @Value("${gemini.api.parameters.seed}")
    private int seed;

    public GenerationConfig map() {
        return GenerationConfig.newBuilder()
                .setCandidateCount(candidateCount)
                .setTemperature(temperature)
                .setMaxOutputTokens(maxOutputTokens)
                .setTopP(topP)
                .setSeed(seed)
                .build();
    }
}
