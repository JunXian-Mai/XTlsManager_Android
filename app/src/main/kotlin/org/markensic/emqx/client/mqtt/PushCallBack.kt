package org.markensic.emqx.client.mqtt

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttMessage

abstract class PushCallBack {
  abstract suspend fun connectionLost(casue: Throwable)

  abstract suspend fun messageArrived(topic: String, message: MqttMessage)

  abstract suspend fun deliveryComplete(token: IMqttDeliveryToken)
}