package page

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import page.chat.ChatPage
import page.setting.SettingPage
import java.awt.event.KeyEvent

private enum class PageRoute {
    Chat, Setting
}

@Composable
@Preview
fun RoutePage() {
    var selection by remember {
        mutableStateOf(PageRoute.values()[0])
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxHeight().weight(1f).background(Color(245, 245, 245))
        ) {
            PageRoute.values().map {
                ActionTab(text = it.name, selected = it == selection) {
                    selection = it
                }
            }
        }

        Box(modifier = Modifier.fillMaxHeight().weight(4f).padding(20.dp)) {
            when(selection) {
                PageRoute.Chat -> {
                    ChatPage()
                }
                PageRoute.Setting -> {
                    SettingPage()
                }
                else -> {
                    Text(selection.name, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun ActionTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .background(if (!selected) Color(245, 245, 245) else Color.White)
        .clickable {
        onClick()
    }) {
        Text(text, modifier = Modifier.align(Alignment.Center))
    }
}