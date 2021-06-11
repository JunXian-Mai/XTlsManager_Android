package org.markensic.emqx.viewmodels

import androidx.lifecycle.ViewModel
import org.markensic.xtls.manager.XTlsFactoryManager
import org.markensic.xtls.hostname.XTlsHostVerifier
import com.markensic.sdk.global.sdkLogd
import com.markensic.sdk.global.sdkLoge
import okhttp3.*
import org.markensic.emqx.cert.impl.XTlsHostnameConfigImpl
import org.markensic.xtls.XTlsKeyManagerBuilder
import org.markensic.xtls.XTlsTrustManagerBuilder
import org.markensic.xtls.constant.ASSETS_PRE
import org.markensic.xtls.etc.Source
import java.io.IOException
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

class TestViewModel : ViewModel() {

  val hostname = XTlsHostVerifier(XTlsHostnameConfigImpl)

  fun mainTest() {
//    doGet()
    emqxTLSLink(true)
  }

  fun doGet() {
    val trustBuilder = XTlsTrustManagerBuilder(Source.NONE())

    val url = "https://www.baidu.com/"
    val sslContext = SSLContext.getInstance("TLSv1.2")
    val trustManager = trustBuilder
      .attachSystemCerts(true)
      .build(hostname)
    sslContext.init(null, arrayOf(trustManager), null)
    val client = OkHttpClient.Builder()
      .sslSocketFactory(sslContext.socketFactory, trustManager)
      .hostnameVerifier(XTlsHostVerifier(XTlsHostnameConfigImpl))
      .build()
    val request = Request.Builder().url(url).method("GET", null).build()
    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        sdkLoge("onFailure: ${e.message}")
      }

      override fun onResponse(call: Call, response: Response) {
        sdkLogd("status Code: ${response.code}")
      }
    })
  }

  fun emqxTLSLink(mutual: Boolean = false) {
    val trustBuilder = XTlsTrustManagerBuilder(Source.PATH())
    val keyBuilder = XTlsKeyManagerBuilder(Source.PATH())

//    val url = "wss://192.168.36.4:8084/mqtt"
    val url = "wss://192.168.36.4:8084/mqtt"
    val trustManager = trustBuilder
      .addPath("${ASSETS_PRE}certs/openssl/rootCA.pem")
      .attachSystemCerts(true)
      .build(hostname)
    var keyManager: KeyManagerFactory? = null
    if (mutual) {
      keyManager = keyBuilder
        .addClientKeyPath(
          "${ASSETS_PRE}certs/openssl/client.pem",
          "${ASSETS_PRE}certs/openssl/client-key.pem"
        )
        .build()
    }

    val sslContext = SSLContext.getInstance("TLSv1.2")
    sslContext.init(
      if (mutual) keyManager?.keyManagers else null,
      arrayOf(trustManager),
      null
    )
    val client = OkHttpClient.Builder()
      .sslSocketFactory(sslContext.socketFactory, trustManager)
      .hostnameVerifier(XTlsHostVerifier(XTlsHostnameConfigImpl))
      .build()
    val request = Request.Builder().url(url).build()
    val webSocket = client.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: Response) {
        sdkLogd("webSocket onOpen")
      }

      override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        sdkLoge("webSocket onFailure ${t.localizedMessage}")
      }
    })
  }
}