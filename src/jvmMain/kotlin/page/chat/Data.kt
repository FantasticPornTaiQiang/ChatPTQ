package page.chat

data class Session(
    val conversations: List<Conversation> = listOf()
)

data class ChatRequest(
    val messages: List<Message>,
    val model: String = /*"gpt-3.5-turbo"*/ "gpt-3.5-turbo"
)

data class Conversation(
    val message: Message,
    val success: Boolean = true
)

data class Message(
    val content: String,
    val role: String = "user",
)
data class ChatResult(
    val choices: List<Choice>,
    val created: Int,
    val id: String,
    val `object`: String,
    val usage: Usage
)

data class Choice(
    val finishReason: String,
    val index: Int,
    val message: Message
)

data class Usage(
    val completionTokens: Int,
    val promptTokens: Int,
    val totalTokens: Int
)
