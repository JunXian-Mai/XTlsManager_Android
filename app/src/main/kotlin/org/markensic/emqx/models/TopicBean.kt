package org.markensic.emqx.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TopicBean(
  val topic: String,
  val qos: Int,
  val content: String,
  val retain: Boolean
) : Parcelable