package com.campbelltech.genaibot.service;

import com.campbelltech.genaibot.model.ChatMessage;

import java.io.IOException;
import java.net.URISyntaxException;

public interface GenAiService {
    ChatMessage predict(String prompt) throws IOException, URISyntaxException;
}
