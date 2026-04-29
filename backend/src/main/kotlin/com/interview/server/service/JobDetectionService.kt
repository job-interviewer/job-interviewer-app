package com.interview.server.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.chat.client.ChatClient
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class JobDetectionService(
    private val chatClient: ChatClient,
    private val objectMapper: ObjectMapper
) {
    private val promptTemplate: String by lazy {
        ClassPathResource("prompts/job-detection.md").inputStream.bufferedReader().readText()
    }

    fun detectJobField(resumeText: String): String {
        val prompt = promptTemplate.replace("{{resume}}", resumeText)
        return try {
            val response = chatClient.prompt()
                .user(prompt)
                .call()
                .content() ?: return "일반 직무"
            val clean = extractJson(response)
            objectMapper.readTree(clean).get("jobField")?.asText() ?: "일반 직무"
        } catch (e: Exception) {
            "일반 직무"
        }
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start >= 0 && end > start) text.substring(start, end + 1) else text
    }
}
