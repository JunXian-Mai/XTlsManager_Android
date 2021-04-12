package org.markensic.emqx.cert.impl

import org.markensic.xtls.constant.Constant
import org.markensic.xtls.impl.XTls509CertSet

object XTls509CertSetImpl : XTls509CertSet {
  override fun getCaCertPaths(): Array<String> = arrayOf(
    "${Constant.ASSETS_PRE}certs/openssl/rootCA.pem"
  )

  override fun getClientKeyCertPathPairs(): Array<XTls509CertSet.ClientKeyPem> = arrayOf(
    XTls509CertSet.ClientKeyPem(
      "${Constant.ASSETS_PRE}certs/openssl/client.pem",
      "${Constant.ASSETS_PRE}certs/openssl/client-key.pem"
    )
  )

  override fun getIgnoreTargetIPVerifierList(): Array<String>  = arrayOf(
    "127.0.0.1"
  )

  override fun getIgnoreTargetHostVerifierList(): Array<String>  = arrayOf(
    ""
  )

  override fun getIgnoreAccessIPVerifierList(): Array<String> = arrayOf(
    "192.168.43.204", "192.168.3.6"
  )

  override fun getIgnoreAccessHostVerifierList(): Array<String>  = arrayOf(
    ""
  )
}