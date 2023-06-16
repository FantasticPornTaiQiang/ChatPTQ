package config

import androidx.compose.runtime.*
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.WindowPlacement
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.launch
import repository.service.UserProxy
import repository.service.retrofitService
import view.LocalAppToaster
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.InputStreamReader

data class AppConfig(
    val enableSystemProxy: Boolean = false,
    val userProxy: UserProxy = UserProxy("0.0.0.0", 0),
    val apiKey: String = "",
    val autoStart: Boolean = false,
    val gptName: String = "小彭",
    val windowPlacement: String = WindowPlacement.Floating.name
)

typealias OnConfigChange = (AppConfig) -> Unit
class AppConfigContext(val appConfig: AppConfig = AppConfig(), val onConfigChange: OnConfigChange)

val LocalAppConfig = compositionLocalOf { AppConfigContext { } }

@Composable
fun AppConfig(onConfigChange: OnConfigChange? = null, App: @Composable () -> Unit) {
    var appConfig by remember { mutableStateOf(AppConfig()) }
    val coroutineScope = rememberCoroutineScope()
    val toast = LocalAppToaster.current

    remember(appConfig) {
        onConfigChange?.invoke(appConfig)
    }

    LaunchedEffect(Unit) {
        val jsonConfig = readConfig() ?: run {
            toast.toastFailure("配置文件未找到")
            return@LaunchedEffect
        }
        applyChanges(null, jsonConfig, onSuccess = {

        }, onFailure = {
            toast.toastFailure(it)
        })
        appConfig = jsonConfig
    }

    CompositionLocalProvider(LocalAppConfig provides AppConfigContext(appConfig) { new ->
        coroutineScope.launch {
            writeConfig(appConfig, new, onSuccess = {

            }, onFailure = {
                toast.toastFailure(it)
            })
            appConfig = new
        }
    }) {
        App()
    }
}

fun readConfig(): AppConfig? {
    val configFile = File("appConfig.json")
    if (!configFile.exists()) {
        if (!configFile.createNewFile()) {
            return null
        }
        var success = false
        writeConfig(null, AppConfig(), onSuccess = {
            success = true
        }, onFailure = {
            success = false
        })
        if (!success) {
            return null
        }
    }
    return try {
        Gson().fromJson(JsonReader(InputStreamReader(FileInputStream(configFile))), AppConfig::class.java)
    } catch (e:Exception) {
        e.printStackTrace()
        null
    }
}
fun writeConfig(oldConfig: AppConfig?, newConfig: AppConfig, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    try {
        val configFile = File("appConfig.json")
        if (!configFile.exists()) {
            if (!configFile.createNewFile()) {
                onFailure("配置失败")
            }
        }
        FileWriter(configFile).run {
            write(Gson().toJson(newConfig))
            close()
        }
        applyChanges(oldConfig, newConfig, onSuccess = {
            onSuccess()
        }, onFailure = onFailure)
    } catch (e: Exception) {
        e.printStackTrace()
        onFailure(e.localizedMessage)
    }
}

fun applyChanges(oldConfig: AppConfig?, newConfig: AppConfig, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    if (oldConfig == null || oldConfig.autoStart != newConfig.autoStart) {
        if (newConfig.autoStart) {
            onFailure("暂不支持开机启动")
        }
    }

    if (oldConfig == null || oldConfig.apiKey != newConfig.apiKey) {
        retrofitService.setApiKey(newConfig.apiKey)
    }

    if (oldConfig == null || oldConfig.userProxy != newConfig.userProxy) {
        if (!newConfig.enableSystemProxy) {
            retrofitService.setProxy(newConfig.userProxy)
        }
    }

    if (oldConfig == null || oldConfig.enableSystemProxy != newConfig.enableSystemProxy) {
        if (newConfig.enableSystemProxy) {
            val proxy = useSystemProxy() ?: run {
                onFailure("暂不支持自动获取系统代理\n请手动配置代理")
                return
            }
            retrofitService.setProxy(proxy)
        } else {
            retrofitService.setProxy(newConfig.userProxy)
        }
    }

    onSuccess()
}

private fun useSystemProxy(): UserProxy? {
//    val props = Properties()
//    val path: Path = Paths.get(System.getProperty("java.home"), "lib", "net.properties")
//    println(System.getProperty("java.home"))
//
//    try {
//        Files.newBufferedReader(path).use { r ->
//            props.load(r)
//            println("props loaded!")
//        }
//    } catch (x: IOException) {
//        System.err.println("props failed loading!")
//        x.printStackTrace(System.err)
//    }
//    // Now you have access to all the net.properties!
//    // Now you have access to all the net.properties!
//    System.out.println(props.getProperty("http.proxyHost"))
////    System.setProperty("java.net.useSystemProxies", "true")
////    val proxyList: List<Proxy>? = ProxySelector.getDefault().select(URI.create())
////    println(proxyList?.joinToString(" ") ?: "null")
////    if (!proxyList.isNullOrEmpty()) {
////        val proxy = proxyList[0]
////        if (proxy.type() != Proxy.Type.HTTP) {
////            return null
////        }
////        val address: InetSocketAddress = proxy.address() as InetSocketAddress
////        val host = address.address.hostAddress
////        val port = address.port
////        println("host: $host, port: $port")
////        return UserProxy(host, port)
////    }
//    println(System.getProperty("http.proxyHost", "NONE"))
    return null
}