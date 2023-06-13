import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import config.AppConfig
import page.RoutePage
import view.Toast


@Composable
@Preview
fun App() {
    MaterialTheme {
        Toast {
            AppConfig {
                RoutePage()
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource("184.png"),
        title = "ChatPTQ",
    ) {
        App()
    }
}