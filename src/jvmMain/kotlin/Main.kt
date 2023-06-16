import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import config.AppConfig
import config.LocalAppConfig
import page.RoutePage
import view.Toast


@Composable
@Preview
fun ApplicationScope.AppWindow() {
    var localConfig by remember { mutableStateOf(AppConfig()) }
    val placement by remember(localConfig) { mutableStateOf(WindowPlacement.valueOf(localConfig.windowPlacement)) }

    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource("184.png"),
        title = "ChatPTQ",
        state = WindowState(placement = placement)
    ) {
        Toast {
            AppConfig(
                onConfigChange = {
                    localConfig = it
                }
            ) {
                MaterialTheme {
                    RoutePage()
                }
            }
        }
    }

}

fun main() = application {
    AppWindow()
}