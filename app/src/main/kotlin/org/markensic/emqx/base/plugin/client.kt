package org.markensic.emqx.base.plugin

import org.markensic.emqx.client.mqtt.Client
import org.markensic.emqx.models.ClientSubjectModel
import org.markensic.emqx.models.TopicBean

fun Client.getSubject(): MutableList<TopicBean>? = ClientSubjectModel.clientSubject[clientId]

fun Client.putSubject(topicBean: TopicBean): Boolean {
  var successPut = false
  val set = getSubject()
  var contains = false
  set?.forEach {
    if (it.topic == topicBean.topic) {
      contains = true
      return@forEach
    }
  }
  if (!contains) {
    if (set != null) {
      ClientSubjectModel.clientSubject[clientId]!!.add(topicBean)
    } else {
      ClientSubjectModel.clientSubject[clientId] = mutableListOf(topicBean)
    }
    successPut = true
  }
  return successPut
}

fun Client.removeSubject(topicBean: TopicBean) {
  val set = getSubject()
  var bean: TopicBean? = null
  if (set != null) {
    set.forEach {
      if (it.topic == topicBean.topic) {
        bean = it
        return@forEach
      }
    }
    bean?.also {
      ClientSubjectModel.clientSubject[clientId]!!.remove(it)
    }
  }
}
