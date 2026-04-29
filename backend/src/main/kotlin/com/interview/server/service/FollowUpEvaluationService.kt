package com.interview.server.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.chat.client.ChatClient
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

data class FollowUpResult(val needsFollowUp: Boolean, val followUpQuestion: String?)

@Service
class FollowUpEvaluationService(
    private val chatClient: ChatClient,
    private val objectMapper: ObjectMapper
) {
    private val promptTemplate: String by lazy {
        ClassPathResource("prompts/followup-evaluation.md").inputStream.bufferedReader().readText()
    }

    fun evaluateAnswer(question: String, answer: String, resume: String): FollowUpResult {
        if (answer.trim().length < 10) {
            return FollowUpResult(true, "조금 더 구체적으로 말씀해주시겠어요?")
        }

        val prompt = promptTemplate
            .replace("{{question}}", question)
            .replace("{{answer}}", answer)
            .replace("{{resume}}", resume)

        var lastException: Exception? = null
        repeat(2) { attempt ->
            try {
                val response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content() ?: return@repeat
                val clean = extractJson(response)
                val node = objectMapper.readTree(clean)
                return FollowUpResult(
                    needsFollowUp = node.get("needsFollowUp")?.asBoolean() ?: false,
                    followUpQuestion = node.get("followUpQuestion")
                        ?.asText()
                        ?.takeIf { it != "null" && it.isNotBlank() }
                )
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw GeminiApiException("답변 평가에 실패했습니다: ${lastException?.message}")
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start >= 0 && end > start) text.substring(start, end + 1) else text
    }
}
