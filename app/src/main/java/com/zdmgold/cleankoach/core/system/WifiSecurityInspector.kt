package com.zdmgold.cleankoach.core.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.zdmgold.cleankoach.core.domain.model.SecurityVerdict
import com.zdmgold.cleankoach.core.domain.model.WifiSecurityReport
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Proxy
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertPathValidator
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateFactory
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

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
                WifiInfo.SECURITY_TYPE_OPEN -> "Open"
                WifiInfo.SECURITY_TYPE_WEP -> "WEP"
                WifiInfo.SECURITY_TYPE_PSK -> "WPA/WPA2"
                WifiInfo.SECURITY_TYPE_EAP -> "WPA-Enterprise"
                WifiInfo.SECURITY_TYPE_SAE -> "WPA3"
                WifiInfo.SECURITY_TYPE_OWE -> "Enhanced Open"
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
            val chain = certs.filterIsInstance<X509Certificate>()
                .filterIndexed { index, cert -> index == 0 || cert.subjectX500Principal != cert.issuerX500Principal }
            if (chain.isEmpty() || anchors.isEmpty()) return SecurityVerdict.UNAVAILABLE

            val trusted = try {
                val path = CertificateFactory.getInstance("X.509").generateCertPath(chain)
                val params = PKIXParameters(anchors).apply { isRevocationEnabled = false }
                CertPathValidator.getInstance("PKIX").validate(path, params)
                true
            } catch (e: CertPathValidatorException) {
                false
            }

            if (trusted) SecurityVerdict.SECURE else SecurityVerdict.DETECTED
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

    private fun systemTrustAnchors(): Set<TrustAnchor> {
        return runCatching {
            val store = KeyStore.getInstance("AndroidCAStore")
            store.load(null, null)
            store.aliases().toList()
                .filter { it.startsWith("system:") }
                .mapNotNull { alias -> (store.getCertificate(alias) as? X509Certificate)?.let { TrustAnchor(it, null) } }
                .toSet()
        }.getOrDefault(emptySet())
    }
}
