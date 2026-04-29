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

    /**
     * Detects the job field from the provided resume text by sending a prompt to the configured LLM and parsing the model's JSON-like response.
     *
     * If no `jobField` can be extracted or an error occurs, returns `"일반 직무"`.
     *
     * @param resumeText The full resume text to include in the prompt.
     * @return The detected `jobField` value from the model response, or `"일반 직무"` as a fallback.
     */
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

    /**
     * Extracts the outermost JSON object from the given text.
     *
     * Finds the first occurrence of '{' and the last occurrence of '}' and returns
     * the substring between them (inclusive). If no valid JSON-like boundaries are
     * found, returns the original text unchanged.
     *
     * @param text The input string that may contain a JSON object.
     * @return The extracted JSON object substring if found, otherwise the original input.
     */
    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start >= 0 && end > start) text.substring(start, end + 1) else text
    }
}
