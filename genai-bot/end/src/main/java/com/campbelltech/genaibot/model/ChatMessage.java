package com.campbelltech.genaibot.model;

import java.util.List;

public record ChatMessage(
        String botName,
        String slogan,
        List<ChatHistory> chatHistory
){ }

