package com.campbelltech.genaibot.mapper;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ParameterMapper {
    
    @Value("${palm.api.parameters.temperature}")
    private double temperature;
    @Value("${palm.api.parameters.max-output-tokens}")
    private int maxOutputTokens;
    @Value("${palm.api.parameters.top-p}")
    private double topP;
    @Value("${palm.api.parameters.top-k}")
    private int topK;

    public com.google.protobuf.Value map() throws InvalidProtocolBufferException {
        // US locale ensures that doubles contains dots (.) and not commas (,)
        Locale.setDefault(Locale.US);
        com.google.protobuf.Value.Builder parameterValueBuilder = com.google.protobuf.Value.newBuilder();
        JsonFormat.parser().merge(String.format("""
                {
                    "temperature": %.1f,
                    "maxOutputTokens": %d,
                    "topP": %.1f,
                    "topK": %d
                }
                """, temperature, maxOutputTokens, topP, topK), parameterValueBuilder);
        return parameterValueBuilder.build();
    }
}
