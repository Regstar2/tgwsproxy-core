package io.github.regstar2.tgwsproxy.core

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

internal interface TgWsNativeLibrary : Library {
    fun StartMtProtoProxy(host: String, port: Int, secret: String, runtimeConfig: String, verbose: Int): Int
    fun StopMtProtoProxy(): Int
    fun GetMtProtoProxyStatus(): Pointer?
    fun GetProxyStatus(): Pointer?
    fun FreeString(pointer: Pointer)
}

internal object NativeBridge {
    private val library: TgWsNativeLibrary by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Native.load("tgwsproxy", TgWsNativeLibrary::class.java)
    }

    fun start(config: TgWsProxyConfig): Int =
        library.StartMtProtoProxy(config.host, config.port, config.secret, config.runtimeConfig, if (config.verbose) 1 else 0)

    fun stop(): Int = library.StopMtProtoProxy()

    fun mtProtoStatus(): String = readString(library.GetMtProtoProxyStatus())

    fun proxyStatus(): String = readString(library.GetProxyStatus())

    private fun readString(pointer: Pointer?): String {
        if (pointer == null) return ""
        return try {
            pointer.getString(0)
        } finally {
            library.FreeString(pointer)
        }
    }
}
