package page.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.Markdown
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

const val sendFastKeyDuration = 500L

@Composable
fun ChatPage() {
    val conversations = rememberSaveable { mutableStateListOf<Conversation>() }

    var inputEnabled by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

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
        if (input.trim() == "") {
            toaster.toastFailure("不能发送空消息")
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
                conversations += Conversation(res.choices[0].message)
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
            items(size) {
                val message = conversations[it].message
                val success = conversations[it].success
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    OutlinedTextField(
                        message.content,
                        onValueChange = {},
                        label = {
                            Text(if (message.role == "assistant") config.gptName else "我",
                                modifier = Modifier
                                    .align(if (message.role == "assistant") Alignment.Start else Alignment.End)
                            )
                        },
                        modifier = Modifier.align(if (message.role == "assistant") Alignment.Start else Alignment.End),
                        isError = !success
                    )
//                    Markdown(message.content, modifier = Modifier.align(Alignment.Start))
                    Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                        Text("${it + 1}/$size")
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
                    .weight(10f)
                    .onPreviewKeyEvent {
                        if (it.key == Key(KeyEvent.VK_ENTER)) {
                            return@onPreviewKeyEvent if (it.type == KeyEventType.KeyDown) {
                                if (sendFastKeyDownTime == 0L) {
                                    sendFastKeyDownTime = System.currentTimeMillis()
                                    false
                                } else {
                                    if (System.currentTimeMillis() - sendFastKeyDownTime > sendFastKeyDuration) {
                                        sendFastKeyDownTime = 0L
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
                    },
                placeholder = {
                    Text("和${config.gptName}聊天...", color = Color.LightGray)
                },
                onValueChange = {
                    if (!inputEnabled || isSending) return@OutlinedTextField
                    input = it
                },
                leadingIcon= if (isSending) {
                    { Loading() }
                } else null
            )
            IconButton(modifier = Modifier.weight(1f), iconPath = "icon_send.png") {
                sendChat()
            }
            IconButton(modifier = Modifier.weight(1f), iconPath = "icon_delete.png") {
                conversations.clear()
            }
        }
    }
}

@Composable
fun IconButton(modifier: Modifier = Modifier, iconPath: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier) {
        Icon(painterResource(iconPath), null, modifier = Modifier.size(20.dp))
    }
}
