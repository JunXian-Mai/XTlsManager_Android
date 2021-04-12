package org.markensic.emqx.viewmodels

import androidx.lifecycle.ViewModel
import org.markensic.xtls.etc.XTlsFactory
import org.markensic.xtls.manager.XTlsHostVerifier
import com.markensic.sdk.global.sdkLogd
import com.markensic.sdk.global.sdkLoge
import okhttp3.*
import org.markensic.emqx.cert.impl.XTls509CertSetImpl
import java.io.IOException
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

class TestViewModel : ViewModel() {
  fun mainTest() {
//    doGet()
    emqxTLSLink(true)
  }

  fun doGet() {
    val url = "https://www.baidu.com/"
    val sslContext = SSLContext.getInstance("TLSv1.2")
    val trustManager = XTlsFactory.creatDefaultManager(XTls509CertSetImpl)
    sslContext.init(null, arrayOf(trustManager), null)
    val client = OkHttpClient.Builder()
      .sslSocketFactory(sslContext.socketFactory, trustManager)
      .hostnameVerifier(XTlsHostVerifier(XTls509CertSetImpl))
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
//    val url = "wss://192.168.43.204:8084/mqtt"
    val url = "wss://192.168.3.6:8084/mqtt"
    val trustManager = XTlsFactory.creatDefaultManager(XTls509CertSetImpl)
    var keyManager: KeyManagerFactory? = null
    if (mutual) {
      keyManager = XTlsFactory.getKeyManagerFactory(
        XTls509CertSetImpl.getClientKeyCertPathPairs()[0]
      )
    }

    val sslContext = SSLContext.getInstance("TLSv1.2")
    sslContext.init(
      if (mutual) keyManager?.keyManagers else null,
      arrayOf(trustManager),
      null
    )
    val client = OkHttpClient.Builder()
      .sslSocketFactory(sslContext.socketFactory, trustManager)
      .hostnameVerifier(XTlsHostVerifier(XTls509CertSetImpl))
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