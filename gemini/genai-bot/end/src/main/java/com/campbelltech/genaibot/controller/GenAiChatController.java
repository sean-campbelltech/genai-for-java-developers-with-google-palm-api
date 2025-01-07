package com.campbelltech.genaibot.controller;

import com.campbelltech.genaibot.model.ChatMessage;
import com.campbelltech.genaibot.service.GenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URISyntaxException;

@Controller
public class GenAiChatController {
    private final GenAiService genAiService;

    @Autowired
    public GenAiChatController(GenAiService genAiService) {
        this.genAiService = genAiService;
    }

    @GetMapping
    public String intro(Model model) throws IOException, URISyntaxException {
        return chat("Who are you and what can you do?", model);
    }

    @PostMapping
    public String chat(@RequestParam(name = "prompt") String prompt, Model model) throws IOException, URISyntaxException {
        ChatMessage chatMessage = genAiService.generateContent(prompt);
        model.addAttribute("model", chatMessage);
        return "genaichat";
    }
}
