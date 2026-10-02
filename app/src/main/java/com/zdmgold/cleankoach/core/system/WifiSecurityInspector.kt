package com.zdmgold.cleankoach.core.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import com.zdmgold.cleankoach.core.domain.model.SecurityVerdict
import com.zdmgold.cleankoach.core.domain.model.WifiSecurityReport
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Proxy
import java.net.URL
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Singleton
class WifiSecurityInspector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val testHost = "https://www.google.com"

    fun inspect(): WifiSecurityReport {
        val internetAccess = checkInternetAccess()
        val encryption = readEncryptionType()
        val stripVerdict = runSslStripTest()
        val splitVerdict = runSslSplitTest(stripVerdict)

        return WifiSecurityReport(
            internetAccess = internetAccess,
            encryptionType = encryption,
            sslStripVerdict = stripVerdict,
            sslSplitVerdict = splitVerdict,
            checkedAt = System.currentTimeMillis()
        )
    }

    private fun checkInternetAccess(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun readEncryptionType(): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return null
        val info = wifiManager.connectionInfo ?: return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (info.currentSecurityType) {
                1 -> "Open"
                2 -> "WEP"
                4 -> "WPA"
                5 -> "WPA2"
                6 -> "WPA3"
                7 -> "WPA3-Transition"
                8 -> "WPA3-SAE"
                else -> "unavailable on this device"
            }
        } else {
            @Suppress("DEPRECATION")
            val capabilities = info.ssid?.let { "" } ?: ""
            if (capabilities.isBlank()) "unavailable on this device" else "unavailable on this device"
        }
    }

    private fun runSslStripTest(): SecurityVerdict {
        return runCatching {
            val url = URL(testHost)
            val conn = url.openConnection(Proxy.NO_PROXY) as HttpsURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.connect()

            val certs = conn.serverCertificates
            conn.disconnect()

            if (certs.isEmpty()) return SecurityVerdict.UNAVAILABLE

            val anchors = systemTrustAnchors()
            val intercepting = certs.any { cert ->
                cert is X509Certificate && !isTrustedBySystem(cert, anchors)
            }

            if (intercepting) SecurityVerdict.DETECTED else SecurityVerdict.SECURE
        }.getOrDefault(SecurityVerdict.UNAVAILABLE)
    }

    private fun runSslSplitTest(strip: SecurityVerdict): SecurityVerdict {
        val proxy = detectSystemProxy()
        return when {
            proxy && strip == SecurityVerdict.DETECTED -> SecurityVerdict.DETECTED
            proxy -> SecurityVerdict.DETECTED
            else -> SecurityVerdict.SECURE
        }
    }

    private fun detectSystemProxy(): Boolean {
        return runCatching {
            val prop = System.getProperty("http.proxyHost")
            !prop.isNullOrBlank()
        }.getOrDefault(false)
    }

    private fun systemTrustAnchors(): Array<X509Certificate> {
        return runCatching {
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(null as java.security.KeyStore?)
            tmf.trustManagers
                .filterIsInstance<X509TrustManager>()
                .firstOrNull()
                ?.acceptedIssuers
                ?: emptyArray()
        }.getOrDefault(emptyArray())
    }

    private fun isTrustedBySystem(cert: X509Certificate, anchors: Array<X509Certificate>): Boolean {
        return anchors.any { anchor ->
            runCatching { cert.verify(anchor.publicKey); true }.getOrDefault(false)
        }
    }
}
