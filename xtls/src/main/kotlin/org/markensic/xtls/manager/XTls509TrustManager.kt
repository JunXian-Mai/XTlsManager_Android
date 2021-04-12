package org.markensic.xtls.manager

import org.markensic.xtls.etc.XTlsFactory
import com.markensic.sdk.global.sdkLogd
import com.markensic.sdk.global.sdkLoge
import org.markensic.xtls.impl.XTls509CertSet
import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

class XTls509TrustManager internal constructor(
  private val trustedSelfCerts: Array<X509Certificate>,
  private val verifier: XTlsHostVerifier,
  private val attachSystemCerts: Boolean = true
) : X509TrustManager {

  // [DEBUG]是否输出证书详细信息
  private val outputCertDetail = false

  // 系统默认证书信任管理器
  private val systemTrustManager = XTlsFactory.getSystemDefaultTrustManager()

  // 信任证书列表
  private val trustedCerts: Array<X509Certificate>

  init {
    if (attachSystemCerts) {
      this.trustedCerts = XTlsFactory.getSystemTrustedCerts()
        .plus(trustedSelfCerts)
    } else {
      this.trustedCerts = trustedSelfCerts
    }

    if (outputCertDetail) {
      // 输出所有信任的证书
      showTrustedCerts()
    }
  }

  override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    checkTrusted(chain, authType, true)
  }

  override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    checkTrusted(chain, authType, false)
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> {
    return trustedCerts
  }

  fun getAcceptedSelfIssuers(): Array<X509Certificate> {
    return trustedSelfCerts
  }

  /**
   * 证书校验
   * @param chain 服务器证书链
   * @param authType 授权类型
   * @param socket 连接socket
   * @param checkClientTrusted 服务端否是校验客户端
   * @throws CertificateException
   */
  @Throws(CertificateException::class)
  private fun checkTrusted(
    chain: Array<out X509Certificate>?,
    authType: String?,
    checkClientTrusted: Boolean
  ) {
    // 校验服务器证书连，类型是否非NULL
    checkTrustedInit(chain, authType)

    // 证书校验
    checkCerts(chain!!, authType!!, checkClientTrusted)
  }

  /**
   * 服务器相关参数非NULL检查
   * @param chain 证书链
   * @param authType 授权类型
   */
  @Throws(IllegalArgumentException::class)
  private fun checkTrustedInit(
    chain: Array<out X509Certificate>?,
    authType: String?
  ) {
    require(chain?.isNotEmpty() == true) { "null or zero-length certificate chain" }
    require(authType?.isNotBlank() == true) { "null or zero-length authentication type" }
  }

  /**
   * 证书校验
   * @param chain 证书链
   * @param authType 授权类型
   * @param checkClientTrusted 是否校验客户端信任
   * @throws CertificateException 校验失败，报错
   */
  @Throws(CertificateException::class)
  private fun checkCerts(
    chain: Array<out X509Certificate>,
    authType: String,
    checkClientTrusted: Boolean
  ) {
    // 是否被自定义证书信任
    if (verifySelfCertificate(chain)) {
//      showTrustedRootCerts(chain[chain.length - 1]);
      return
    }
    if (attachSystemCerts) {
      // 使用系统默认的证书管理器校验证书
      if (checkClientTrusted) {
        systemTrustManager.checkClientTrusted(chain, authType)
      } else {
        systemTrustManager.checkServerTrusted(chain, authType)
      }
    } else {
      throw CertificateException("no certificate in root path")
    }

//    showTrustedRootCerts(chain[chain.length - 1]);
  }

  /**
   * 校验自定义证书
   * @param chain 证书链
   * @return 校验成功
   */
  private fun verifySelfCertificate(chain: Array<out X509Certificate>): Boolean {
    // 循环判断证书链是否存在自定义信任证书
    for (chainCert in chain) {
      for (cert in trustedSelfCerts) {
        try {
          chainCert.verify(cert.publicKey)
          // 某一级证书被信任，直接信任整个证书链
          return true
        } catch (e: Exception) {
          // 打印自定义证书校验失败信息
          println("self certs verify error: " + e.message)
        }
      }
    }
    // 不被自定义证书信任
    return false
  }

  private fun showTrustedCerts() {
    if (outputCertDetail) {
      for (cert in trustedCerts) {
        println("adding as trusted cert:")
        println("  Subject: " + cert.subjectX500Principal)
        println("  Issuer: " + cert.issuerX500Principal)
        println("  Algorithm: " + cert.publicKey.algorithm)
        println("  Serial number: " + cert.serialNumber.toString(16))
        println("  Valid from " + cert.notBefore + " until " + cert.notAfter)
      }
    }
  }

  private fun showTrustedRootCerts(rootCert: X509Certificate) {
    if (outputCertDetail) {
      println("Found trusted certificate: $rootCert")
    }
  }
}

