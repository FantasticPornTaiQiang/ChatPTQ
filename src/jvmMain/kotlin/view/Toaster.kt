package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface Toaster {
    fun toast(message: String) = toast(message, 2500L, true)

    fun toast(message: String, duration: Long) = toast(message, duration, true)

    fun toast(message: String, duration: Long, success: Boolean)

    fun toastFailure(message: String) = toast(message, 2500L, false)

    companion object {
        val Default = object : Toaster {
            override fun toast(message: String, duration: Long, success: Boolean) { }
        }
    }
}

val LocalAppToaster = compositionLocalOf { Toaster.Default }

private val toastColors = arrayOf(
    Color(223, 245, 245),
    Color(255, 233, 233),
)

@Composable
fun Toast(App: @Composable () -> Unit) {
    var showToast by remember { mutableStateOf(false) }
    var toastColor by remember { mutableStateOf(toastColors[0]) }
    val coroutineScope = rememberCoroutineScope()
    var toastText by remember { mutableStateOf("") }
    val toaster by remember {
        mutableStateOf(object : Toaster {
            override fun toast(message: String, duration: Long, success: Boolean) {
                if (showToast) {
                    return
                }
                coroutineScope.launch {
                    toastText = message
                    toastColor = toastColors[if (success) 0 else 1]
                    showToast = true
                    delay(duration)
                    showToast = false
                }
            }
        })
    }

    CompositionLocalProvider(LocalAppToaster provides toaster) {
        Box(modifier = Modifier.fillMaxSize()) {
            App()

            AnimatedVisibility(showToast, modifier = Modifier.align(Alignment.Center)) {
                Box(modifier = Modifier.wrapContentSize().clip(shape = RoundedCornerShape(6.dp)).background(toastColor)) {
                    Text(toastText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
}