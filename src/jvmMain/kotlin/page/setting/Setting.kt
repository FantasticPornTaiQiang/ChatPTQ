package page.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import config.AppConfig
import config.LocalAppConfig
import config.OnConfigChange
import kotlinx.coroutines.launch
import repository.api.AccountService
import repository.service.request
import util.parseDateFromUnixTime
import view.Loading
import view.LocalAppToaster
import java.lang.module.ModuleFinder
import java.util.Date


@Composable
fun SettingPage() {
    LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.Start) {
        item {
            Mine(LocalAppConfig.current.appConfig, LocalAppConfig.current.onConfigChange)
            Proxy(LocalAppConfig.current.appConfig, LocalAppConfig.current.onConfigChange)
            ApiKey(LocalAppConfig.current.appConfig, LocalAppConfig.current.onConfigChange)
            System(LocalAppConfig.current.appConfig, LocalAppConfig.current.onConfigChange)
        }
    }
}

@Composable
fun Mine(appConfig: AppConfig, onChange: OnConfigChange) {
    SettingPanel("个人设置") {
        OutlinedTextField(
            appConfig.gptName,
            singleLine = true,
            onValueChange = {
                if (it.length < 15) {
                    onChange(appConfig.copy(gptName = it))
                }
            }, label = {
                Text("GPT昵称")
            }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun Proxy(appConfig: AppConfig, onChange: OnConfigChange) {
    var enableSystemProxy by remember(appConfig) {
        mutableStateOf(appConfig.enableSystemProxy)
    }

    var userProxy by remember(appConfig) {
        mutableStateOf(appConfig.userProxy)
    }

    remember(enableSystemProxy, userProxy) {
        onChange(appConfig.copy(enableSystemProxy = enableSystemProxy, userProxy = userProxy))
    }

    SettingPanel("代理设置") {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
            enableSystemProxy = !enableSystemProxy
        }, horizontalArrangement = Arrangement.Start) {
            Checkbox(enableSystemProxy, modifier = Modifier.size(40.dp), onCheckedChange = {
                enableSystemProxy = it
            })
            Text("系统代理", modifier = Modifier.padding(start = 5.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
            enableSystemProxy = !enableSystemProxy
        }, horizontalArrangement = Arrangement.Start) {
            Checkbox(!enableSystemProxy, modifier = Modifier.size(40.dp), onCheckedChange = {
                enableSystemProxy = !it
            })
            Text("手动代理", modifier = Modifier.padding(start = 5.dp))
        }

        if (!enableSystemProxy) {
            Row(modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth()) {
                OutlinedTextField(userProxy.hostname, modifier = Modifier.weight(4f), singleLine = true, onValueChange = {
                    userProxy = userProxy.copy(hostname = it)
                }, label = {
                    Text("地址")
                })
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(userProxy.port.toString(), modifier = Modifier.weight(1f), singleLine = true, onValueChange = {
                    if (it.contains("\\D".toRegex()) || it.trim() == "") {
                        return@OutlinedTextField
                    }
                    userProxy = userProxy.copy(port = it.toInt())
                }, label = {
                    Text("端口")
                })
            }
        }
    }
}

@Composable
fun ApiKey(appConfig: AppConfig, onChange: OnConfigChange) {
    val coroutineScope = rememberCoroutineScope()
    var total by remember { mutableStateOf<Double?>(null) }
    var used by remember { mutableStateOf<Double?>(null) }
    var expireTime by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val toaster = LocalAppToaster.current

    SettingPanel("请求设置") {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(appConfig.apiKey,
                label = {
                    Text("APIKey")
                },
                onValueChange = {
                    onChange(appConfig.copy(apiKey = it))
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    if (isLoading) return@OutlinedButton
                    isLoading = true
                    coroutineScope.launch {
                        request<AccountService> {
                            val dailyCost = dailyCost()
                            val bill = billSubscription()
                            total = bill.hard_limit_usd
                            used = dailyCost.total_usage / 100
                            expireTime = parseDateFromUnixTime(bill.access_until.toLong())
                        }.whatEver {
                            isLoading = false
                        }.toastException(toaster)
                    }
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent
                )) {
                    Text("余量查询")
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Loading()
                }

                if (expireTime != null) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Text("过期时间：${expireTime}", fontSize = 14.sp)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 3.dp)
                , verticalAlignment = Alignment.CenterVertically) {
                if (total != null) {
                    Text("总额度：${total}$", fontSize = 14.sp)
                }

                if (used != null) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Text("已使用(近100天)：${used}$", fontSize = 14.sp)
                }

                if (total != null && used != null) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Text("剩余额度(近100天)：${total!! - used!!}$", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun System(appConfig: AppConfig, onChange: OnConfigChange) {
    SettingPanel("系统设置") {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
            onChange(appConfig.copy(autoStart = !appConfig.autoStart))
        }, horizontalArrangement = Arrangement.Start) {
            Checkbox(appConfig.autoStart, modifier = Modifier.size(40.dp), onCheckedChange = {
                onChange(appConfig.copy(autoStart = it))
            })
            Text("开机启动", modifier = Modifier.padding(start = 5.dp))
        }
    }
}

@Composable
fun SettingPanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, fontWeight = FontWeight.W600, fontSize = 16.sp, modifier = Modifier.padding(start = 2.dp))

    Spacer(modifier = Modifier.height(8.dp))

    Column(modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(8.dp)).background(Color(245, 245, 245)).padding(vertical = 4.dp, horizontal = 8.dp)) {
        content()
    }

    Spacer(modifier = Modifier.height(16.dp))
}