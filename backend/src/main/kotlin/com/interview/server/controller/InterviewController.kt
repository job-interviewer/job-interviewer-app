package com.interview.server.controller

import com.interview.server.dto.*
import com.interview.server.service.InterviewService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/interview")
class InterviewController(
    private val interviewService: InterviewService
) {
    /**
     * Starts a new interview session from a JSON payload.
     *
     * The request may include a cover letter text and a flag to enable follow-up questions.
     * The response contains the created session's ID, job field, and a list of questions with 1-based order indices.
     *
     * @param request The JSON payload containing `coverLetterText` and `followUpEnabled`.
     * @return A StartInterviewResponse with `sessionId`, `jobField`, and `questions` (each mapped to QuestionDto with `orderIndex` starting at 1).
     */
    @PostMapping("/start", consumes = ["application/json"])
    fun startInterviewJson(@RequestBody request: StartInterviewRequest): StartInterviewResponse {
        val session = interviewService.startInterview(
            coverLetterText = request.coverLetterText,
            file = null,
            followUpEnabled = request.followUpEnabled
        )
        return StartInterviewResponse(
            sessionId = session.sessionId,
            jobField = session.jobField,
            questions = session.questions.mapIndexed { i, q ->
                QuestionDto(questionId = q.questionId, content = q.content, orderIndex = i + 1)
            }
        )
    }

    /**
     * Starts a new interview session using an uploaded file and returns the created session data.
     *
     * The response contains the session identifier, the detected job field, and the session's questions
     * where each question's `orderIndex` is 1-based.
     *
     * @param file The uploaded file used to initialize the interview (for example, a resume or cover letter attachment).
     * @param followUpEnabled Whether follow-up questions are enabled for the session.
     * @return A [StartInterviewResponse] containing `sessionId`, `jobField`, and the list of questions with 1-based `orderIndex`.
     */
    @PostMapping("/start", consumes = ["multipart/form-data"])
    fun startInterviewMultipart(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("followUpEnabled", defaultValue = "true") followUpEnabled: Boolean
    ): StartInterviewResponse {
        val session = interviewService.startInterview(
            coverLetterText = null,
            file = file,
            followUpEnabled = followUpEnabled
        )
        return StartInterviewResponse(
            sessionId = session.sessionId,
            jobField = session.jobField,
            questions = session.questions.mapIndexed { i, q ->
                QuestionDto(questionId = q.questionId, content = q.content, orderIndex = i + 1)
            }
        )
    }

    /**
     * Submits an answer for a question in the specified interview session.
     *
     * @param sessionId Identifier of the interview session.
     * @param request Request payload containing `questionId` and the `answer`.
     * @return A `SubmitAnswerResponse` representing the result of the submission.
     */
    @PostMapping("/{sessionId}/answer")
    fun submitAnswer(
        @PathVariable sessionId: String,
        @RequestBody request: SubmitAnswerRequest
    ): SubmitAnswerResponse = interviewService.submitAnswer(sessionId, request.questionId, request.answer)

    /**
         * Marks the interview session identified by `sessionId` as complete and returns the completion result.
         *
         * @param sessionId The unique identifier of the interview session to complete.
         * @return The `CompleteInterviewResponse` containing the outcome and any final data for the completed session.
         */
        @PostMapping("/{sessionId}/complete")
    fun completeInterview(@PathVariable sessionId: String): CompleteInterviewResponse =
        interviewService.completeInterview(sessionId)
}
