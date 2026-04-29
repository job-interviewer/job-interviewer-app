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

    @PostMapping("/{sessionId}/answer")
    fun submitAnswer(
        @PathVariable sessionId: String,
        @RequestBody request: SubmitAnswerRequest
    ): SubmitAnswerResponse = interviewService.submitAnswer(sessionId, request.questionId, request.answer)

    @PostMapping("/{sessionId}/complete")
    fun completeInterview(@PathVariable sessionId: String): CompleteInterviewResponse =
        interviewService.completeInterview(sessionId)
}
