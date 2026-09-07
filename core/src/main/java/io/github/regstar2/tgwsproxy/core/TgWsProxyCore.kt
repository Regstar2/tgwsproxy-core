package io.github.regstar2.tgwsproxy.core

private val rawSecretPattern = Regex("^[0-9a-fA-F]{32}$")

data class TgWsProxyConfig(
    val host: String = "127.0.0.1",
    val port: Int = 1443,
    val secret: String,
    val runtimeConfig: String = "",
    val verbose: Boolean = false,
) {
    internal fun validationError(): String? {
        if (host.isBlank()) return "host must not be blank"
        if (port !in 1..65535) return "port must be in 1..65535"
        if (!rawSecretPattern.matches(secret.trim())) return "secret must be a 32-character hexadecimal MTProto secret"
        return null
    }

    internal fun normalized(): TgWsProxyConfig = copy(
        host = host.trim(),
        secret = secret.trim().lowercase(),
        runtimeConfig = runtimeConfig.trim(),
    )

    override fun toString(): String =
        "TgWsProxyConfig(host=$host, port=$port, secret=<redacted>, runtimeConfigLength=${runtimeConfig.length}, verbose=$verbose)"
}

enum class TgWsProxyRuntimeState {
    DISABLED,
    STARTING,
    LISTENING_LOCAL_ONLY,
    LISTENING_ROUTE_READY,
    FAILED_PORT_IN_USE,
    FAILED_INVALID_SECRET,
    FAILED_RUNTIME_ERROR,
    STOPPED,
    NATIVE_UNAVAILABLE,
    UNKNOWN;

    companion object {
        internal fun fromNative(value: String?): TgWsProxyRuntimeState =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}

enum class TgWsProxyError {
    INVALID_CONFIG,
    NATIVE_UNAVAILABLE,
    START_FAILED,
    STOP_FAILED,
}

data class TgWsProxyStatus(
    val state: TgWsProxyRuntimeState,
    val host: String = "",
    val port: Int = 0,
    val outbound: String = "",
    val selectedBackend: String = "",
    val actualBackend: String = "",
    val fallbackUsed: Boolean = false,
    val routeReason: String = "",
    val activeConnections: Long = 0,
    val totalConnections: Long = 0,
    val lastError: String = "",
    val secretFingerprint: String = "",
    val fields: Map<String, String> = emptyMap(),
)

data class TgWsProxyOperationResult(
    val success: Boolean,
    val nativeCode: Int? = null,
    val error: TgWsProxyError? = null,
    val message: String = "",
    val status: TgWsProxyStatus? = null,
)

object TgWsProxyCore {
    @Synchronized
    fun start(config: TgWsProxyConfig): TgWsProxyOperationResult {
        val normalized = config.normalized()
        normalized.validationError()?.let {
            return TgWsProxyOperationResult(false, error = TgWsProxyError.INVALID_CONFIG, message = it)
        }

        return try {
            val code = NativeBridge.start(normalized)
            val status = status()
            if (code == 0) {
                TgWsProxyOperationResult(true, nativeCode = code, status = status)
            } else {
                TgWsProxyOperationResult(
                    false,
                    nativeCode = code,
                    error = if (code == -2) TgWsProxyError.INVALID_CONFIG else TgWsProxyError.START_FAILED,
                    message = startErrorMessage(code),
                    status = status,
                )
            }
        } catch (error: Throwable) {
            nativeUnavailable(error)
        }
    }

    @Synchronized
    fun stop(): TgWsProxyOperationResult = try {
        val code = NativeBridge.stop()
        val status = status()
        if (code == 0) {
            TgWsProxyOperationResult(true, nativeCode = code, status = status)
        } else {
            TgWsProxyOperationResult(false, nativeCode = code, error = TgWsProxyError.STOP_FAILED, message = "native runtime stop failed: code=$code", status = status)
        }
    } catch (error: Throwable) {
        nativeUnavailable(error)
    }

    fun status(): TgWsProxyStatus = try {
        parseNativeStatus(NativeBridge.mtProtoStatus())
    } catch (error: Throwable) {
        TgWsProxyStatus(TgWsProxyRuntimeState.NATIVE_UNAVAILABLE, lastError = error.message.orEmpty())
    }

    fun transportStatus(): Map<String, String> = try {
        parseStatusFields(NativeBridge.proxyStatus())
    } catch (_: Throwable) {
        emptyMap()
    }

    private fun nativeUnavailable(error: Throwable) = TgWsProxyOperationResult(
        false,
        error = TgWsProxyError.NATIVE_UNAVAILABLE,
        message = "native runtime is unavailable: ${error.javaClass.simpleName}",
        status = TgWsProxyStatus(TgWsProxyRuntimeState.NATIVE_UNAVAILABLE, lastError = error.message.orEmpty()),
    )

    private fun startErrorMessage(code: Int): String = when (code) {
        -2 -> "native runtime rejected the MTProto secret"
        -3 -> "local MTProto port is already in use"
        -4 -> "native runtime configuration or startup failed"
        else -> "native runtime start failed: code=$code"
    }
}

internal fun parseNativeStatus(raw: String): TgWsProxyStatus {
    val fields = parseStatusFields(raw)
    return TgWsProxyStatus(
        state = TgWsProxyRuntimeState.fromNative(fields["status"]),
        host = fields["host"].orEmpty(),
        port = fields["port"]?.toIntOrNull() ?: 0,
        outbound = fields["outbound"].orEmpty(),
        selectedBackend = fields["selected_backend"].orEmpty(),
        actualBackend = fields["actual_backend"].orEmpty(),
        fallbackUsed = fields["fallback_used"].toBooleanStrictOrNull() ?: false,
        routeReason = fields["route_reason"].orEmpty(),
        activeConnections = fields["active"]?.toLongOrNull() ?: 0,
        totalConnections = fields["total"]?.toLongOrNull() ?: 0,
        lastError = fields["last_error"].orEmpty(),
        secretFingerprint = fields["secret_fingerprint"].orEmpty(),
        fields = fields,
    )
}

internal fun parseStatusFields(raw: String): Map<String, String> {
    if (raw.isBlank()) return emptyMap()
    return buildMap {
        raw.split(';').forEach { token ->
            val separator = token.indexOf('=')
            if (separator > 0) put(token.substring(0, separator), token.substring(separator + 1))
        }
    }
}
