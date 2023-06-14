package repository.service

import config.AppConfig
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import view.Toaster
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

data class UserProxy(val hostname: String, val port: Int)

interface ApiService

const val baseUrl = "https://api.openai.com/"

class RetrofitService {
    private var userProxy = AppConfig().userProxy

    private var apiKey = AppConfig().apiKey

    private var okhttpClient = buildOkHttpClient()

    var retrofit: Retrofit = buildRetrofit()
        private set

    fun setProxy(proxy: UserProxy) {
        userProxy = proxy
        okhttpClient = buildOkHttpClient()
        retrofit = buildRetrofit()
    }

    fun setApiKey(newApiKey: String) {
        apiKey = newApiKey
        okhttpClient = buildOkHttpClient()
        retrofit = buildRetrofit()
    }

    private fun buildOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(200000L, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                chain.request()
                    .newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                    .let {
                        chain.proceed(it)
                    }
            }
            .addInterceptor { chain ->
                chain.request().newBuilder().build().let {
                    chain.proceed(it).also { res ->
                        println("request:${res.request().url()}\ncode: ${res.code()}\nmessage:${res.message()}\nbody:\n${res.peekBody(1024 * 1024).string()}")
                    }
                }
            }
            .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(userProxy.hostname, userProxy.port)))
            .build()
    }

    private fun buildRetrofit() = Retrofit.Builder()
        .baseUrl("${baseUrl}v1/")
        .client(okhttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

val retrofitService: RetrofitService = RetrofitService()

data class ResultExtra(val e: Exception? = null) {
    fun toastException(toaster: Toaster): ResultExtra {
        e?.let {
            with(toaster) {
                when(it) {
                    is HttpException -> {
                        when (it.code()) {
                            401 -> toastFailure("无效ApiKey")
                            else -> toastFailure(it.localizedMessage)
                        }
                    }
                    is SocketTimeoutException -> {
                        toastFailure("响应超时")
                    }
                    else -> {
                        toastFailure(e.localizedMessage)
                    }
                }
            }
        }
        return this
    }

    inline fun whatEver(block: () -> Unit): ResultExtra {
        block()
        return this
    }

    inline fun success(block: () -> Unit): ResultExtra {
        if (e == null) {
            block()
        }
        return this
    }

    inline fun failure(block: () -> Unit): ResultExtra {
        e?.let {
            block()
        }
        return this
    }
}

typealias ServiceScope<reified T> = suspend T.() -> Unit

suspend inline fun <reified T : ApiService> request(crossinline serviceScope: ServiceScope<T>): ResultExtra {
    try {
        retrofitService.retrofit.create(T::class.java).serviceScope()
    } catch (e: Exception) {
        e.printStackTrace()
        return ResultExtra(e)
    }
    return ResultExtra()
}

//inline fun <reified T : ApiService> CoroutineScope.request(toaster: Toaster? = null, crossinline serviceScope: ServiceScope<T>) {
//    launch {
//        val res = request(serviceScope)
//        toaster?.let {
//            res.toastException(it)
//        }
//    }
//}
