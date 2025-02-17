/*
 * Copyright (c) 2025. PortSwigger Ltd. All rights reserved.
 *
 * This code may be used to extend the functionality of Burp Suite Community Edition
 * and Burp Suite Professional, provided that this usage does not violate the
 * license terms for those products.
 */

package ai;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.EnhancedCapability;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import static burp.api.montoya.EnhancedCapability.AI_FEATURES;
import static java.util.concurrent.Executors.newFixedThreadPool;

@SuppressWarnings("unused")
public class Extension implements BurpExtension {
    public static final String SYSTEM_MESSAGE = 
        "You are AI HTTP ANALYZER, an advanced security analysis assistant integrated into Burp Suite. " +
        "Your role is to examine HTTP requests and responses for potential security vulnerabilities, " +
        "such as SQL injection, XSS, CSRF, and other threats. " +
        "Provide a focused technical analysis including: " +
        "1. Quick identification of detected vulnerabilities " +
        "2. Clear technical steps for exploitation " +
        "3. PoC examples and payloads where applicable " +
        "Keep responses concise and technical, focusing on exploitation methods. " + 
        "Avoid theoretical discussions or lengthy explanations. " +
        "Additionally, provide direct answers to any user questions or inputs related to security testing.";

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("AI HTTP ANALYZER");

        Logging logging = api.logging();

        ExecutorService executorService = newFixedThreadPool(5);
        PromptHandler promptHandler = new PromptHandler(logging, api.ai(), SYSTEM_MESSAGE);

        BurpAITab burpAITab = new BurpAITab(api.userInterface(), logging, promptHandler, executorService);

        api.userInterface().registerSuiteTab("AI HTTP ANALYZER", burpAITab.getUiComponent());
        api.userInterface().registerContextMenuItemsProvider(new BurpAIContextMenu(burpAITab));
        api.extension().registerUnloadingHandler(executorService::shutdownNow);

        // Log custom success message with logToOutput
        logging.logToOutput("AI HTTP ANALYZER extension loaded successfully.\nAuthor: ALPEREN ERGEL (@alpernae)\nVersion: 2025.1.0");
    }

    @Override
    public Set<EnhancedCapability> enhancedCapabilities() {
        return Set.of(AI_FEATURES);
    }
}