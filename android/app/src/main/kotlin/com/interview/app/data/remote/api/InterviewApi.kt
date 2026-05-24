package com.interview.app.data.remote.api

import com.interview.app.data.remote.dto.CompleteInterviewResponse
import com.interview.app.data.remote.dto.StartInterviewRequest
import com.interview.app.data.remote.dto.StartInterviewResponse
import com.interview.app.data.remote.dto.SubmitAnswerRequest
import com.interview.app.data.remote.dto.SubmitAnswerResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface InterviewApi {
    /**
     * Starts a new interview session using the provided request data.
     *
     * @param request Data required to create and configure the interview session.
     * @return A Retrofit `Response` containing a `StartInterviewResponse` when the request is successful; non-successful HTTP responses are represented by a non-successful `Response`.
     */
    @POST("api/interview/start")
    suspend fun startInterview(@Body request: StartInterviewRequest): Response<StartInterviewResponse>

    /**
     * Starts an interview by uploading a multipart file alongside a follow-up enabled flag.
     *
     * @param file The multipart file part to upload for starting the interview.
     * @param followUpEnabled A `RequestBody` part named "followUpEnabled" indicating whether follow-up questions are enabled.
     * @return A Retrofit `Response` containing `StartInterviewResponse` on success or HTTP error information on failure.
     */
    @Multipart
    @POST("api/interview/start")
    suspend fun startInterviewWithFile(
        @Part file: MultipartBody.Part,
        @Part("followUpEnabled") followUpEnabled: RequestBody
    ): Response<StartInterviewResponse>

    /**
     * Submits an answer for the given interview session.
     *
     * @param sessionId The interview session identifier.
     * @param request The answer submission payload.
     * @return The HTTP response containing a `SubmitAnswerResponse` with the submission result.
     */
    @POST("api/interview/{sessionId}/answer")
    suspend fun submitAnswer(
        @Path("sessionId") sessionId: String,
        @Body request: SubmitAnswerRequest
    ): Response<SubmitAnswerResponse>

    /**
     * Completes the interview identified by the given session ID.
     *
     * @param sessionId The interview session identifier to complete.
     * @return The HTTP response containing a CompleteInterviewResponse with the interview completion result.
     */
    @POST("api/interview/{sessionId}/complete")
    suspend fun completeInterview(
        @Path("sessionId") sessionId: String
    ): Response<CompleteInterviewResponse>
}
