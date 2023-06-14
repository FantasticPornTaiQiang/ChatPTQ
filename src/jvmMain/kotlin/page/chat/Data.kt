package page.chat

data class Session(
    val conversations: List<Conversation> = listOf()
)

data class ChatRequest(
    val messages: List<Message>,
    val model: String = "gpt-3.5-turbo"
)

data class Conversation(
    val message: Message,
    val tokenUsage: Int? = null,
    val success: Boolean = true,
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
    val finish_reason: String,
    val index: Int,
    val message: Message
)

data class Usage(
    val completion_tokens: Int,
    val prompt_tokens: Int,
    val total_tokens: Int
)
