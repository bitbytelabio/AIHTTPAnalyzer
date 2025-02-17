/*
 * Copyright (c) 2025. PortSwigger Ltd. All rights reserved.
 *
 * This code may be used to extend the functionality of Burp Suite Community Edition
 * and Burp Suite Professional, provided that this usage does not violate the
 * license terms for those products.
 */

package ai;

import burp.api.montoya.ai.Ai;
import burp.api.montoya.ai.chat.Message;
import burp.api.montoya.ai.chat.PromptException;
import burp.api.montoya.ai.chat.PromptResponse;
import burp.api.montoya.logging.Logging;

import static burp.api.montoya.ai.chat.Message.systemMessage;
import static burp.api.montoya.ai.chat.Message.userMessage;

public class PromptHandler
{
    private final Logging logging;
    private final Ai ai;
    private final Message systemMessage;

    public PromptHandler(Logging logging, Ai ai, String systemPrompt)
    {
        this.logging = logging;
        this.ai = ai;
        this.systemMessage = systemMessage(systemPrompt);
    }

    public Message[] build(String userPrompt)
    {
        return new Message[]{systemMessage, userMessage(userPrompt)};
    }

    public PromptResponse sendWithSystemMessage(String userPrompt)
    {
        if (ai.isEnabled())
        {
            try
            {
                return ai.prompt().execute(build(userPrompt));
            }
            catch (PromptException e)
            {
                logging.logToError(e);
            }
        }

        throw new RuntimeException("Please enable AI functionality.");
    }
}
