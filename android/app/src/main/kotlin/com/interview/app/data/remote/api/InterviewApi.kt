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
    @POST("api/interview/start")
    suspend fun startInterview(@Body request: StartInterviewRequest): Response<StartInterviewResponse>

    @Multipart
    @POST("api/interview/start")
    suspend fun startInterviewWithFile(
        @Part file: MultipartBody.Part,
        @Part("followUpEnabled") followUpEnabled: RequestBody
    ): Response<StartInterviewResponse>

    @POST("api/interview/{sessionId}/answer")
    suspend fun submitAnswer(
        @Path("sessionId") sessionId: String,
        @Body request: SubmitAnswerRequest
    ): Response<SubmitAnswerResponse>

    @POST("api/interview/{sessionId}/complete")
    suspend fun completeInterview(
        @Path("sessionId") sessionId: String
    ): Response<CompleteInterviewResponse>
}
