package repository.api

import page.chat.ChatRequest
import page.chat.ChatResult
import page.chat.ModelData
import repository.service.ApiService
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChatCompletionService : ApiService {
    @POST("chat/completions")
    suspend fun chat(@Body body: ChatRequest): ChatResult

    @GET("models")
    suspend fun listModels(): ModelData
}
