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

    /**
     * Evaluate whether a follow-up question is needed for a candidate's answer.
     *
     * Short answers (trimmed length < 10) immediately produce a follow-up suggestion.
     *
     * @param question The interview question asked to the candidate.
     * @param answer The candidate's answer to evaluate.
     * @param resume Contextual resume information for the candidate.
     * @return A [FollowUpResult] where `needsFollowUp` indicates whether a follow-up is required and
     *         `followUpQuestion` contains the suggested follow-up text or `null` if none.
     * @throws GeminiApiException If the AI evaluation fails after retrying, with the last error message.
     */
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

    /**
     * Extracts the first JSON object substring from the given text.
     *
     * @param text Input string that may contain JSON embedded in surrounding text.
     * @return The substring containing the first JSON object (including its enclosing `{}`) if a matching pair of braces is found with a closing brace after the opening brace; otherwise returns the original `text`.
     */
    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start >= 0 && end > start) text.substring(start, end + 1) else text
    }
}
