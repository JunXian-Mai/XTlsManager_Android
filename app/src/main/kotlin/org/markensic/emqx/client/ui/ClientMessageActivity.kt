package org.markensic.emqx.client.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.markensic.emqx.R
import org.markensic.emqx.base.BaseActivity
import org.markensic.emqx.base.constant.KeyConstant
import org.markensic.emqx.client.mqtt.PushCallBack
import org.markensic.emqx.client.widget.SubjectTopicDialog
import org.markensic.emqx.models.ConnectBean
import org.markensic.emqx.models.TopicBean
import org.markensic.emqx.viewmodels.MessageViewModel
import com.markensic.sdk.utils.ToastUtils
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.markensic.emqx.databinding.ActivityClientMessageBinding

class ClientMessageActivity : BaseActivity() {

  lateinit var binding: ActivityClientMessageBinding
  val viewModel: MessageViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_client_message)
    binding.viewmodel = viewModel
    viewModel.connectBean = getConnectBean()

    observeFiled()

    setEvent()

    connect()
  }

  private fun getConnectBean(): ConnectBean = intent.getParcelableExtra<ConnectBean>(KeyConstant.CONNECT_BEAN)!!

  private fun connect() {
    val callBack = object : PushCallBack() {
      override suspend fun messageArrived(topic: String, message: MqttMessage) {
        binding.messageGroup.addView(
          viewModel.getArrivedMessageTextView(this@ClientMessageActivity, topic, message) {
            val o = it.getTag(R.id.topic_bean)
            if (o is TopicBean) {
              viewModel.fillMessage(o)
            }
          }
        )
        binding.messageGroup.postDelayed({
          (binding.messageGroup.parent as NestedScrollView).fullScroll(View.FOCUS_DOWN)
        }, 250)
      }

      override suspend fun deliveryComplete(token: IMqttDeliveryToken) {
        if (token.isComplete) {
          binding.messageGroup.addView(
            viewModel.getSendMessageTextView(this@ClientMessageActivity) {
              val o = it.getTag(R.id.topic_bean)
              if (o is TopicBean) {
                viewModel.fillMessage(o)
              }
            }
          )
          binding.message.setText("")
          binding.messageGroup.postDelayed({
            (binding.messageGroup.parent as NestedScrollView).fullScroll(View.FOCUS_DOWN)
          }, 250)
        } else {
          val e = token.exception
          ToastUtils.show(
            """
              reason: ${e.reasonCode}
              msg: ${e.message}
              loc: ${e.localizedMessage}
              cause: ${e.cause}
              excep: $e
            """.trimIndent()
          )
        }
      }

      override suspend fun connectionLost(casue: Throwable) {
        doConnect(this)
        viewModel.reSubscribe()
      }
    }

    doConnect(callBack)
  }

  private fun doConnect(callBack: PushCallBack) {
    try {
      viewModel.connect(callBack)
    } catch (e: Exception) {
      finish()
      ToastUtils.show(
        """
        msg: ${e.message}
        loc: ${e.localizedMessage}
        cause: ${e.cause}
        excep: $e
      """.trimIndent()
      )
    }
  }

  private fun observeFiled() {
    viewModel.publishTopic.observe(this, Observer<String> {
      if (binding.messageTopic.text.toString() != it) {
        binding.messageTopic.setText(it)
      }
    })

    viewModel.publishQos.observe(this, Observer<String> {
      if (binding.messageQos.text.toString() != it) {
        binding.messageQos.setText(it)
      }
    })

    viewModel.publishMessage.observe(this, Observer<String> {
      if (binding.message.text.toString() != it) {
        binding.message.setText(it)
      }
    })

    viewModel.publishRetain.observe(this, Observer<Boolean> {
      if (binding.retain.isChecked != it) {
        binding.retain.isChecked = it
      }
    })
  }

  private fun setEvent() {
    binding.add.setOnClickListener {
      SubjectTopicDialog.Builder(this@ClientMessageActivity).setConfirmListener { topic, qos ->
        try {
          if (viewModel.subscribe(topic, qos.toInt())) {
            binding.subjectGroup.addView(
              viewModel.getSubjectTopicListText(
                this@ClientMessageActivity,
                "$topic    qos:$qos", onclick = {
                  binding.messageTopic.setText(topic)
                  binding.messageQos.setText(qos)
                }, cancelListener = { v, parnet ->
                  try {
                    viewModel.unsubscribe(topic)
                  } catch (e: Exception) {
                    val error = """
                      msg: ${e.message}
                      loc: ${e.localizedMessage}
                      cause: ${e.cause}
                      excep: $e
                    """.trimIndent()
                    ToastUtils.show(error)
                  }
                })
            )
          }
        } catch (e: Exception) {
          ToastUtils.show(
            """
              msg: ${e.message}
              loc: ${e.localizedMessage}
              cause: ${e.cause}
              excep: $e
            """.trimIndent()
          )
        }

      }.show()
    }

    binding.send.setOnClickListener {
      try {
        viewModel.publish()
      } catch (e: Exception) {
        ToastUtils.show(
          """
            msg: ${e.message}
            loc: ${e.localizedMessage}
            cause: ${e.cause}
            excep: $e
          """.trimIndent()
        )
      }
    }
  }

  override fun finish() {
    try {
      viewModel.disconnect()
    } catch (e: Exception) {
      ToastUtils.show(
        """
          msg: ${e.message}
          loc: ${e.localizedMessage}
          cause: ${e.cause}
          excep: $e
        """.trimIndent()
      )
    }
    super.finish()
  }
}