package io.github.regstar2.tgwsproxy.core

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

internal interface TgWsNativeLibrary : Library {
    fun StartMtProtoProxy(host: String, port: Int, secret: String, runtimeConfig: String, verbose: Int): Int
    fun StopMtProtoProxy(): Int
    fun GetMtProtoProxyStatus(): Pointer?
    fun GetProxyStatus(): Pointer?
    fun ConfigureAWGWarp(configPath: String, enabled: Int, preferred: Int, allowFallback: Int): Int
    fun ResetAWGWarp(): Int
    fun GenerateWireGuardKeyPair(): Pointer?
    fun ValidateAWGWarpConfig(configPath: String): Int
    fun ProbeAWGWarpConfig(configPath: String, target: String, timeoutMillis: Long): Pointer?
    fun RegisterConsumerWARPDirect(publicKey: String, timeoutMillis: Long): Pointer?
    fun ActivateConsumerWARPDirect(registrationID: String, token: String, timeoutMillis: Long): Pointer?
    fun CheckConsumerWARPWorker(workerBase: String, timeoutMillis: Long): Pointer?
    fun RegisterConsumerWARPWithWorker(workerBase: String, publicKey: String, timeoutMillis: Long): Pointer?
    fun ActivateConsumerWARPWithWorker(workerBase: String, registrationID: String, token: String, timeoutMillis: Long): Pointer?
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

    fun configureAwgWarp(configPath: String, enabled: Boolean, preferred: Boolean, allowFallback: Boolean): Int =
        library.ConfigureAWGWarp(configPath, if (enabled) 1 else 0, if (preferred) 1 else 0, if (allowFallback) 1 else 0)

    fun resetAwgWarp(): Int = library.ResetAWGWarp()

    fun generateWireGuardKeyPairJson(): String = readString(library.GenerateWireGuardKeyPair())

    fun validateAwgWarpConfig(configPath: String): Int = library.ValidateAWGWarpConfig(configPath)

    fun probeAwgWarpConfigJson(configPath: String, target: String, timeoutMillis: Long): String =
        readString(library.ProbeAWGWarpConfig(configPath, target, timeoutMillis))

    fun registerConsumerWarpDirectJson(publicKey: String, timeoutMillis: Long): String =
        readString(library.RegisterConsumerWARPDirect(publicKey, timeoutMillis))

    fun activateConsumerWarpDirectJson(registrationID: String, token: String, timeoutMillis: Long): String =
        readString(library.ActivateConsumerWARPDirect(registrationID, token, timeoutMillis))

    fun checkConsumerWarpWorkerJson(workerBase: String, timeoutMillis: Long): String =
        readString(library.CheckConsumerWARPWorker(workerBase, timeoutMillis))

    fun registerConsumerWarpWithWorkerJson(workerBase: String, publicKey: String, timeoutMillis: Long): String =
        readString(library.RegisterConsumerWARPWithWorker(workerBase, publicKey, timeoutMillis))

    fun activateConsumerWarpWithWorkerJson(workerBase: String, registrationID: String, token: String, timeoutMillis: Long): String =
        readString(library.ActivateConsumerWARPWithWorker(workerBase, registrationID, token, timeoutMillis))

    private fun readString(pointer: Pointer?): String {
        if (pointer == null) return ""
        return try {
            pointer.getString(0)
        } finally {
            library.FreeString(pointer)
        }
    }
}
