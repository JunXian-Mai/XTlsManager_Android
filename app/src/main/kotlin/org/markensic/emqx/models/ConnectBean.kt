package org.markensic.emqx.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConnectBean(
  val name: String,
  val clientId: String,
  val address: String,
  val user: String,
  val password: String,
  val keepAlive: Int,
  val cleanSession: Boolean,
  val enableTls: Boolean,
  val oneWayCertification: Boolean,
  val twoWayCertification: Boolean,
  val ca: String,
  val clientPem: String,
  val clientKey: String
) : Parcelable