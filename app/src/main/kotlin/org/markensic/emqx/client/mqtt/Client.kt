package org.markensic.emqx.client.mqtt

import org.markensic.emqx.base.plugin.putSubject
import org.markensic.emqx.base.plugin.removeSubject
import org.markensic.xtls.hostname.XTlsHostVerifier
import org.markensic.emqx.models.TopicBean
import com.markensic.sdk.global.sdkLogd
import com.markensic.sdk.global.sdkLoge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.markensic.emqx.cert.impl.XTlsHostnameConfigImpl
import javax.net.ssl.SSLSocketFactory

class Client(
  val broker: String,
  val clientId: String,
  persistence: MemoryPersistence = MemoryPersistence()
) {
  private val client = MqttClient(broker, clientId, persistence)
  private val connOpts = MqttConnectOptions()

  fun setUserName(userName: String) =
    this.apply {
      connOpts.userName = userName
    }

  fun setPassword(password: String) =
    this.apply {
      connOpts.password = password.toCharArray()
    }

  fun setKeepAlive(alive: Int) =
    this.apply {
      connOpts.keepAliveInterval = alive
    }

  fun setCleanSession(isClean: Boolean) =
    this.apply {
      connOpts.isCleanSession = isClean
    }

  fun setSSLSocketFactory(sslSocketFactory: SSLSocketFactory) =
    this.apply {
      connOpts.socketFactory = sslSocketFactory
      connOpts.sslHostnameVerifier = XTlsHostVerifier(XTlsHostnameConfigImpl)
    }

  fun setCallBack(callback: PushCallBack) =
    this.apply {
      client.setCallback(object : MqttCallback {
        override fun connectionLost(cause: Throwable) {
          sdkLoge("连接已经断开，重连......")
          GlobalScope.launch(Dispatchers.Main) {
            callback.connectionLost(cause)
          }
        }

        override fun messageArrived(topic: String, message: MqttMessage) {
          sdkLogd(
            """
              接收到的消息主题: $topic
              接收到的消息Qos: ${message.qos}
              接收到的消息内容: ${String(message.payload)}
            """.trimIndent()
          )
          GlobalScope.launch(Dispatchers.Main) {
            callback.messageArrived(topic, message)
          }
        }

        override fun deliveryComplete(token: IMqttDeliveryToken) {
          sdkLogd("deliveryComplete---------${token.isComplete}")
          GlobalScope.launch(Dispatchers.Main) {
            callback.deliveryComplete(token)
          }
        }

      })
    }

  fun connect() {
    client.connect(connOpts)
  }

  fun subscribe(subTopic: String, qos: Int = 1): Boolean {
    client.subscribe(subTopic, qos)
    return putSubject(
      TopicBean(
        subTopic, qos, "", false
      )
    )
  }

  fun unsubscribe(subTopic: String) {
    client.unsubscribe(subTopic)
    removeSubject(
      TopicBean(
        subTopic, 0, "", false
      )
    )
  }

  fun publish(pubTopic: String, content: String, qos: Int, isRetained: Boolean) {
    val message = MqttMessage(content.toByteArray())
    message.qos = qos
    message.isRetained = isRetained
    client.publish(pubTopic, message)
  }

  fun disconnect() {
    client.disconnect()
  }

  fun close() {
    client.close()
  }
}