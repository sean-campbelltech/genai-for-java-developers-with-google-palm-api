package com.campbelltech.genaibot.service;

import com.campbelltech.genaibot.model.ChatMessage;

import java.io.IOException;
import java.net.URISyntaxException;

public interface GenAiService {
    ChatMessage generateContent(String prompt) throws IOException, URISyntaxException;
}
