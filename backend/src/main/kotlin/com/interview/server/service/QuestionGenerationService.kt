package com.interview.server.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.interview.server.model.InterviewQuestion
import org.springframework.ai.chat.client.ChatClient
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class QuestionGenerationService(
    private val chatClient: ChatClient,
    private val objectMapper: ObjectMapper
) {
    private val promptTemplate: String by lazy {
        ClassPathResource("prompts/question-generation.md").inputStream.bufferedReader().readText()
    }

    /**
     * Generates interview questions tailored to the provided resume and job field.
     *
     * Builds a prompt from the service's template, requests generation from the chat client, extracts a JSON array from the response, and returns the parsed list of InterviewQuestion objects. The method attempts generation up to two times before failing.
     *
     * @param resumeText Resume text used to contextualize the generated questions.
     * @param jobField Target job field used to tailor question content and categories.
     * @return A list of generated InterviewQuestion objects; contains at least one question when successful.
     * @throws GeminiApiException if the chat response is missing, no valid questions are produced, or all attempts fail.
     */
    fun generateQuestions(resumeText: String, jobField: String): List<InterviewQuestion> {
        val prompt = promptTemplate
            .replace("{{resume}}", resumeText)
            .replace("{{jobField}}", jobField)

        var lastException: Exception? = null
        repeat(2) { attempt ->
            try {
                val response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content() ?: throw GeminiApiException("질문 생성 응답이 없습니다.")
                val clean = extractJsonArray(response)
                val node = objectMapper.readTree(clean)
                if (node.isArray && node.size() > 0) {
                    return node.mapIndexed { idx, q ->
                        InterviewQuestion(
                            questionId = q.get("questionId")?.asText() ?: "q${idx + 1}",
                            content = q.get("content")?.asText() ?: "",
                            category = q.get("category")?.asText() ?: "직무역량"
                        )
                    }
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw GeminiApiException("질문 생성에 실패했습니다: ${lastException?.message}")
    }

    /**
     * Extracts the first JSON array found in the given text.
     *
     * @return The substring containing the JSON array (from the first '[' to the last ']'), or the original text if no valid array is found.
     */
    private fun extractJsonArray(text: String): String {
        val start = text.indexOf('[')
        val end = text.lastIndexOf(']')
        return if (start >= 0 && end > start) text.substring(start, end + 1) else text
    }
}
