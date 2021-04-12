package org.markensic.emqx.models

object ClientSubjectModel {
  val subjectTopics = mutableListOf<TopicBean>()

  val clientSubject = mutableMapOf<String, MutableList<TopicBean>>()


}