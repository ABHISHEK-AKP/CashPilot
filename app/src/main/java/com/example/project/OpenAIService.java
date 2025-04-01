package com.example.project;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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"
    })
    @POST("v1/chat/completions")
    Call<OpenAIResponse> getChatResponse(@Body OpenAIRequest request);
}