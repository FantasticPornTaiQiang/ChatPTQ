package page.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import config.FastSendMode
import config.LocalAppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import repository.api.ChatCompletionService
import repository.data_store.DSKey
import repository.data_store.getFromDataStore
import repository.data_store.saveToDataStore
import repository.service.request
import util.copyToClipboard
import view.Loading
import view.LocalAppToaster
import java.awt.event.KeyEvent

@Composable
fun ChatPage() {
    val conversations = remember { mutableStateListOf<Conversation>() }

    var inputEnabled by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var inputCopy by remember { mutableStateOf("") }

    val scrollState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    val toaster = LocalAppToaster.current
    val config = LocalAppConfig.current.appConfig
    var sendFastKeyDownTime by remember { mutableStateOf(0L) }

    remember(conversations.size) {
        coroutineScope.launch {
            delay(200)
            scrollState.animateScrollToItem(conversations.size)
        }
    }

    DisposableEffect(Unit) {
        val session = DSKey.chatConversations.getFromDataStore(default = Session())
        conversations.clear()
        conversations += session.conversations

        onDispose {
            Session(conversations).saveToDataStore(DSKey.chatConversations)
        }
    }

    val sendChat = sendChat@{
        if (input.isBlank()) {
            toaster.toastFailure("请输入内容")
            return@sendChat
        }

        if (isSending) return@sendChat

        coroutineScope.launch {
            isSending = true
            //去掉因Enter快捷键产生的空行
            input = input.replace("((\r\n)|\n)[\\s\t ]*(\\1)+".toRegex(), "\n").trim()

            val message = Message(content = input)
            conversations += Conversation(message)

            request<ChatCompletionService> {
                val res = chat(ChatRequest(messages = conversations.filter { it.success }.map { it.message }))
                val last = conversations.removeLast()
                conversations += last.copy(tokenUsage = res.usage.prompt_tokens)
                conversations += Conversation(res.choices[0].message, res.usage.completion_tokens)
            }.failure {
                val last = conversations.removeLast()
                conversations += last.copy(success = false)
            }.success {
                input = ""
            }.whatEver {
                isSending = false
            }.toastException(toaster)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = scrollState, modifier = Modifier.fillMaxWidth().weight(6f)) {
            val size = conversations.size
            items(size) { conversationIndex ->
                val conversation = conversations[conversationIndex]
                val message = conversation.message
                val success = conversation.success
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedTextField(
                        message.content,
                        onValueChange = {},
                        label = {
                            Text((if (message.role == "assistant") config.gptName else "我") + if (conversation.tokenUsage == null) "" else " (${conversation.tokenUsage}tks)",
                                modifier = Modifier
                                    .align(if (message.role == "assistant") Alignment.Start else Alignment.End)
                            )
                        },
                        modifier = Modifier.align(if (message.role == "assistant") Alignment.Start else Alignment.End),
                        isError = !success
                    )
                    Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                        Text("${conversationIndex + 1}/$size", fontSize = 14.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.width(5.dp))

                        IconButton(iconPath = "icon_translate_to_eng.png") {
                            input = "Translate the following into English please:\n${message.content}"
                            sendChat()
                        }

                        IconButton(iconPath = "icon_translate.png") {
                            input = "Translate the following into Chinese please:\n${message.content}"
                            sendChat()
                        }

                        IconButton(iconPath = "icon_copy.png") {
                            copyToClipboard(message.content)
                        }
                    }
                }
            }
        }

        Divider(
            modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
            color = Color(233, 233, 233),
            thickness = 1.5.dp)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().weight(1f)) {
            OutlinedTextField(
                input,
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent {
                        when(FastSendMode.valueOf(config.fastSendMode)) {
                            FastSendMode.LongPressEnter -> {
                                if (it.key == Key(KeyEvent.VK_ENTER)) {
                                    return@onPreviewKeyEvent if (it.type == KeyEventType.KeyDown) {
                                        if (sendFastKeyDownTime == 0L) {
                                            sendFastKeyDownTime = System.currentTimeMillis()
                                            false
                                        } else {
                                            if (System.currentTimeMillis() - sendFastKeyDownTime > config.fastSendLongPressDuration) {
                                                sendFastKeyDownTime = 0L
                                                input = inputCopy
                                                sendChat()
                                                true
                                            } else {
                                                inputEnabled = false
                                                false
                                            }
                                        }
                                    } else if (it.type == KeyEventType.KeyUp) {
                                        sendFastKeyDownTime = 0L
                                        inputEnabled = true
                                        false
                                    } else false
                                }
                                false
                            }
//                            FastSendMode.ControlEnter -> {
//                                if (it.key == Key(KeyEvent.VK_ENTER) && it.type == KeyEventType.KeyUp) {
//                                    if (!it.isCtrlPressed) {
//                                        input = inputCopy
//                                        sendChat()
//                                        true
//                                    } else {
//                                        input = inputCopy + "\n"
//                                        false
//                                    }
//                                } else false
//                            }
//                            FastSendMode.ShiftEnter -> {
//                                if (!it.isShiftPressed && it.key == Key(KeyEvent.VK_ENTER) && it.type == KeyEventType.KeyUp) {
//                                    input = inputCopy
//                                    sendChat()
//                                    true
//                                } else false
//                            }
                            else -> {
                                false
                            }
                        }
                    },
                placeholder = {
                    Text("和${config.gptName}聊天...", color = Color.LightGray)
                },
                onValueChange = {
                    if (!inputEnabled || isSending) return@OutlinedTextField
                    inputCopy = input
                    input = it
                },
                leadingIcon= if (isSending) {
                    { Loading() }
                } else null
            )
            Box(modifier = Modifier.size(80.dp)) {
                IconButton(modifier = Modifier.align(Alignment.TopStart), iconPath = "icon_send.png") {
                    sendChat()
                }
                IconButton(modifier = Modifier.align(Alignment.TopEnd), iconPath = "icon_delete.png") {
                    conversations.clear()
                }
                IconButton(modifier = Modifier.align(Alignment.BottomStart), iconPath = "icon_translate.png") {
                    if (input.isBlank()) {
                        toaster.toastFailure("请输入翻译内容")
                        return@IconButton
                    }
                    input = "翻译成中文:\n$input"
                    sendChat()
                }
                IconButton(modifier = Modifier.align(Alignment.BottomEnd), iconPath = "icon_translate_to_eng.png") {
                    if (input.isBlank()) {
                        toaster.toastFailure("请输入翻译内容")
                        return@IconButton
                    }
                    input = "Translate the following into English please:\n$input"
                    sendChat()
                }
            }
        }
    }
}

@Composable
fun IconButton(modifier: Modifier = Modifier, iconPath: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier.size(40.dp)
    ) {
        Icon(painterResource(iconPath), null, modifier = Modifier.size(18.dp))
    }
}