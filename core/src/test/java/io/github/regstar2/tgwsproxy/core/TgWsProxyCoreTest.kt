package io.github.regstar2.tgwsproxy.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TgWsProxyCoreTest {
    @Test
    fun configDoesNotExposeSecretInToString() {
        val secret = "00112233445566778899aabbccddeeff"
        val config = TgWsProxyConfig(secret = secret)
        assertFalse(config.toString().contains(secret))
        assertTrue(config.toString().contains("<redacted>"))
    }

    @Test
    fun statusParserMapsNativeFrontendState() {
        val status = parseNativeStatus(
            "status=LISTENING_ROUTE_READY;host=127.0.0.1;port=1443;outbound=READY;" +
                "selected_backend=cf_proxy_ws;actual_backend=cf_proxy_ws;fallback_used=false;" +
                "route_reason=selected;active=2;total=7;last_error=;secret_fingerprint=0123456789ab"
        )
        assertEquals(TgWsProxyRuntimeState.LISTENING_ROUTE_READY, status.state)
        assertEquals(1443, status.port)
        assertEquals("cf_proxy_ws", status.selectedBackend)
        assertEquals(2L, status.activeConnections)
        assertEquals(7L, status.totalConnections)
    }
}
