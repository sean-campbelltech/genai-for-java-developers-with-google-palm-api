package com.campbelltech.genaibot.mapper;

import com.google.common.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class InstanceMapper {
    
    @org.springframework.beans.factory.annotation.Value("${palm.bot.context}")
    private String context;

    public List<Value> map(String prompt) throws IOException, URISyntaxException {
        Value.Builder instanceValue = Value.newBuilder();
        String json = String.format("""
                            {
                                "context": "%s",  
                                "examples": %s,                                             
                                "messages": [ 
                                    {
                                        "author": "%s",
                                        "content": "%s"
                                    }
                                ]
                            }
                            """, context, getExamples(), "User", prompt);
        JsonFormat.parser().merge(json, instanceValue);
        List<Value> instances = new ArrayList<>();
        instances.add(instanceValue.build());
        return instances;
    }

    private String getExamples() throws IOException, URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("examples.json").toURI());
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
