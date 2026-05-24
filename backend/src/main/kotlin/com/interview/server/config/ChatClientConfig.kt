package com.interview.server.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {
    /**
     * Creates a ChatClient bean from the supplied builder and exposes it to the Spring context.
     *
     * @param builder The ChatClient.Builder used to construct the ChatClient.
     * @return The constructed ChatClient instance.
     */
    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient = builder.build()
}
