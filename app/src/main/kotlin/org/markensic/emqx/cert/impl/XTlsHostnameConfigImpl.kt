package org.markensic.emqx.cert.impl

import org.markensic.xtls.hostname.XTlsHostnameConfig

object XTlsHostnameConfigImpl : XTlsHostnameConfig {
  override fun getIgnoreTargetIPVerifierList(): Array<String>  = arrayOf(
    "127.0.0.1"
  )

  override fun getIgnoreTargetHostVerifierList(): Array<String>  = arrayOf(
    ""
  )

  override fun getIgnoreAccessIPVerifierList(): Array<String> = arrayOf(
    "192.168.43.204", "192.168.36.4"
  )

  override fun getIgnoreAccessHostVerifierList(): Array<String>  = arrayOf(
    ""
  )
}