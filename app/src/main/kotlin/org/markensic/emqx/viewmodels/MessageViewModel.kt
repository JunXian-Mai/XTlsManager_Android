package org.markensic.emqx.viewmodels

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.markensic.sdk.ui.dp
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.markensic.emqx.R
import org.markensic.emqx.base.plugin.getSubject
import org.markensic.emqx.cert.impl.XTls509CertSetImpl
import org.markensic.emqx.client.mqtt.Client
import org.markensic.emqx.client.mqtt.PushCallBack
import org.markensic.emqx.client.widget.DeleteTextView
import org.markensic.emqx.models.ConnectBean
import org.markensic.emqx.models.TopicBean
import org.markensic.xtls.etc.XTlsFactory
import org.markensic.xtls.impl.XTls509CertSet
import org.markensic.xtls.manager.XTlsHostVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

class MessageViewModel : ViewModel() {

  var connectBean: ConnectBean? = null
  private var client: Client? = null

  val publishTopic = MutableLiveData("")
  val publishQos = MutableLiveData("")
  val publishMessage = MutableLiveData("")
  val publishRetain = MutableLiveData(false)

  fun fillMessage(topic: TopicBean) {
    publishTopic.value = topic.topic
    publishQos.value = topic.qos.toString()
    publishMessage.value = topic.content
    publishRetain.value = topic.retain
  }

  @Throws(Exception::class)
  fun connect(callBack: PushCallBack) {
    try {
      connectBean?.let {
        client = Client(it.address, it.clientId)
          .setUserName(it.user)
          .setPassword(it.password)
          .setKeepAlive(it.keepAlive)
          .setCleanSession(it.cleanSession)
          .setCallBack(callBack)
          .apply {
            if (it.enableTls) {
              this.setSSLSocketFactory(getSSLSocketFactory(it))
            }
          }
        client?.connect() ?: throw RuntimeException("Connect Error: Create Client exception")
      } ?: throw RuntimeException("Connect Error: Connect Params exception")
    } catch (e: Exception) {
      throw e
    }
  }

  private fun getSSLSocketFactory(bean: ConnectBean, protocol: String = "TLSv1.2"): SSLSocketFactory {
    val sslContext = SSLContext.getInstance(protocol)
    val trustManager = XTlsFactory.creatManager(
      XTlsFactory.getCaCertificates(arrayOf(bean.ca)),
      XTlsHostVerifier(XTls509CertSetImpl)
    )
    sslContext.init(
      if (bean.twoWayCertification)
        XTlsFactory.getKeyManagerFactory(
          XTls509CertSet.ClientKeyPem(
            bean.clientPem, bean.clientKey
          )
        ).keyManagers
      else
        null,
      arrayOf(trustManager),
      null
    )
    return sslContext.socketFactory
  }

  fun MutableLiveData<String>.get() = this.value ?: ""
  fun MutableLiveData<Boolean>.get() = this.value ?: false


  fun getSubjectTopicListText(
    c: Context,
    topic: String,
    onclick: ((View) -> Unit)? = null,
    cancelListener: ((v: View, parent: ViewGroup) -> Unit)? = null
  ): DeleteTextView {
    val params =
      LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    return getDeleteTextView(
      c,
      params,
      topic,
      onclick = onclick,
      isSingleLine = true,
      isSetPadding = false,
      textColor = Color.GRAY,
      textSize = 13f,
      cancelListener = cancelListener
    )
  }

  fun getSendMessageTextView(c: Context, onclick: ((View) -> Unit)? = null): DeleteTextView {
    val message = "Topic: ${publishTopic.get()}  Qos: ${publishQos.get()}\nContent:\n${publishMessage.get()}"
    val params =
      LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    params.gravity = Gravity.RIGHT
    params.setMargins(0, 5.dp.toInt(), 0, 0)
    val v = getDeleteTextView(c, params, message, R.drawable.send_green_bg, onclick, textColor = Color.WHITE)
    v.setTag(
      R.id.topic_bean,
      TopicBean(publishTopic.get(), publishQos.get().toInt(), publishMessage.get(), publishRetain.get())
    )
    return v
  }

  fun getArrivedMessageTextView(
    c: Context,
    topic: String,
    msg: MqttMessage,
    onclick: ((View) -> Unit)? = null
  ): DeleteTextView {
    val params =
      LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    params.gravity = Gravity.LEFT
    params.setMargins(0, 5.dp.toInt(), 0, 0)
    val message = "Topic: $topic  Qos: ${msg.qos}\nContent:\n${String(msg.payload)}"
    val v = getDeleteTextView(c, params, message, R.drawable.arrived_gray_bg, onclick)
    v.setTag(R.id.topic_bean, TopicBean(topic, msg.qos, String(msg.payload), msg.isRetained))
    return v
  }

  private fun getDeleteTextView(
    c: Context,
    p: LinearLayout.LayoutParams,
    msg: String,
    resDrawableId: Int = 0,
    onclick: ((View) -> Unit)? = null,
    isSingleLine: Boolean = false,
    isSetPadding: Boolean = true,
    textColor: Int = Color.BLACK,
    textSize: Float = 16f,
    cancelListener: ((v: View, parent: ViewGroup) -> Unit)? = null
  ): DeleteTextView {
    val v = DeleteTextView(c, null)
    v.text = msg
    v.layoutParams = p
    v.textSize = textSize
    v.setTextColor(textColor)
    if (resDrawableId != 0) {
      v.background = c.resources.getDrawable(resDrawableId)
    }
    if (isSetPadding) {
      v.setPadding(
        v.paddingLeft + 10.dp.toInt(),
        v.paddingTop + 3.dp.toInt(),
        v.paddingRight + 3.dp.toInt(),
        v.paddingBottom + 10.dp.toInt()
      )
    }
    v.isSingleLine = isSingleLine
    v.setCancelListener { v, parent ->
      parent.removeView(v)
      cancelListener?.invoke(v, parent)
    }
    v.setOnLongClickListener {
      v.showDeleteImage = true
      return@setOnLongClickListener true
    }
    v.setOnClickListener {
      onclick?.invoke(v)
    }
    return v
  }

  fun reSubscribe() {
    client!!.getSubject()?.forEach {
      client!!.subscribe(it.topic, it.qos)
    }
  }

  fun subscribe(topic: String, qos: Int) = client!!.subscribe(topic, qos)

  fun unsubscribe(subTopic: String) {
    client!!.unsubscribe(subTopic)
  }

  fun publish() {
    client!!.publish(publishTopic.get(), publishMessage.get(), publishQos.get().toInt(), publishRetain.get())
  }

  fun disconnect() {
    client?.disconnect()
  }

  fun close() {
    client?.close()
  }
}