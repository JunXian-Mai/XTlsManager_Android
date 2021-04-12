package org.markensic.emqx.base.plugin

fun getRandomString(length: Int): String {
  val base = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
  val sb = StringBuffer()
  for (i in 0 until length) {
    val number = base.indices.random()
    sb.append(base[number])
  }
  return sb.toString()
}