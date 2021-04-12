package org.markensic.xtls.etc

import android.os.Build
import android.util.Base64
import com.markensic.sdk.utils.AssetsUtils
import org.markensic.xtls.constant.Constant
import org.markensic.xtls.impl.XTls509CertSet
import org.markensic.xtls.manager.XTls509ExtendedTrustManager
import org.markensic.xtls.manager.XTls509TrustManager
import org.markensic.xtls.manager.XTlsHostVerifier
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object XTlsFactory {

  private val caFactory = CertificateFactory.getInstance("X.509")

  /**
   * 根据配置的密钥集创建证书管理器
   *
   * @param certSet 自定义密钥集
   * @param attachSystemCerts 是否添加系统证书
   * @return 证书管理器
   */
  fun creatDefaultManager(certSet: XTls509CertSet, attachSystemCerts: Boolean = true): X509TrustManager {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      XTls509ExtendedTrustManager(
        getCaCertificates(certSet.getCaCertPaths()),
        XTlsHostVerifier(certSet),
        attachSystemCerts
      )
    } else {
      XTls509TrustManager(
        getCaCertificates(certSet.getCaCertPaths()),
        XTlsHostVerifier(certSet),
        attachSystemCerts
      )
    }
  }


  /**
   * 根据配置的密钥集创建证书管理器
   *
   * @param trustedSelfCerts 自定义Ca证书集
   * @param verifier 域名校验器
   * @param attachSystemCerts 是否添加系统证书
   * @return 证书管理器
   */
  fun creatManager(
    trustedSelfCerts: Array<X509Certificate>,
    verifier: XTlsHostVerifier,
    attachSystemCerts: Boolean = true
  ): X509TrustManager {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      XTls509ExtendedTrustManager(
        trustedSelfCerts,
        verifier,
        attachSystemCerts
      )
    } else {
      XTls509TrustManager(
        trustedSelfCerts,
        verifier,
        attachSystemCerts
      )
    }
  }

  /**
   * 获取Android System 中默认的证书信任库，信任的根证列表
   * 获取指定目录的文件导出信任根证列表
   * @return System 信任根证列表
   */
  fun getSystemTrustedCerts(): Array<X509Certificate> {
    val dir = File(Constant.SYSTEM_CERT_DIR)
    if (dir.exists() && dir.isDirectory) {
      dir.listFiles()?.map {
        FileInputStream(it).use { caIns ->
          caFactory.generateCertificate(caIns) as X509Certificate
        }
      }?.let {
        return it.toTypedArray()
      }
    }
    return arrayOf()
  }

  /**
   * 根据信任的证书路径列表获取信任的证书列表
   * @param caCertPath 信任的证书路径列表
   * @return 指定路径下的证书列表
   */
  fun getCaCertificates(certPaths: Array<String>): Array<X509Certificate> {
    return certPaths.map {
      if (it.startsWith(Constant.ASSETS_PRE)) {
        val assetsName = it.replace(Constant.ASSETS_PRE, "")
        AssetsUtils.open(name = assetsName) { ins ->
          caFactory.generateCertificate(ins) as X509Certificate
        }
      } else {
        caFactory.generateCertificate(FileInputStream(it)) as X509Certificate
      }
    }.toTypedArray()
  }

  /**
   * 根据密钥文件路径，获取密钥文件中的密钥
   * @param certKeyPath 密钥文件路径
   * @return 密钥文件中的密钥
   */
  fun getCertPrivateKey(certKeyPath: String): PrivateKey {
    val builder = StringBuilder()
    if (certKeyPath.startsWith(Constant.ASSETS_PRE)) {
      val assetsName = certKeyPath.replace(Constant.ASSETS_PRE, "")
      AssetsUtils.open(name = assetsName) {
        val br = BufferedReader(InputStreamReader(it))
        br.useLines { lines ->
          lines.forEach { line ->
            if (line[0] != '-') {
              builder.append(line)
              builder.append('\r')
            }
          }
        }
      }
    } else {
      FileInputStream(certKeyPath).use { keyIns ->
        val br = BufferedReader(InputStreamReader(keyIns))
        br.useLines {
          it.forEach { line ->
            if (line[0] != '-') {
              builder.append(line)
              builder.append('\r')
            }
          }
        }
      }
    }
    val buffer = Base64.decode(builder.toString(), Base64.NO_WRAP)
    val keySpec = PKCS8EncodedKeySpec(buffer)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
  }

  /**
   * 根据客户端证书及密钥路径封装类，生成对应密钥的密钥管理器
   * @param keyCertPathPair 客户端证书及密钥路径封装类
   * @param password 密钥密码
   * @return 密钥管理器
   */
  fun getKeyManagerFactory(
    keyCertPathPair: XTls509CertSet.ClientKeyPem,
    password: String = ""
  ): KeyManagerFactory {
    return KeyManagerFactory.getInstance("PKIX").apply {
      val clientCert = getCaCertificates(arrayOf(keyCertPathPair.cert))[0]
      val clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
      clientKeyStore.load(null, null)
      clientKeyStore.setCertificateEntry("certificate", clientCert)
      clientKeyStore.setKeyEntry(
        "private-key",
        getCertPrivateKey(keyCertPathPair.key),
        password.toCharArray(),
        arrayOf<Certificate>(clientCert)
      )
      init(clientKeyStore, password.toCharArray())
    }
  }

  /**
   * 获取系统的默认证书信任管理器
   * @return 系统默认证书信任管理器
   */
  @Throws(IllegalStateException::class)
  fun getSystemDefaultTrustManager(): X509TrustManager {
    val tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
      init(null as KeyStore?)
    }
    var manager: X509TrustManager? = null
    tmFactory.trustManagers.forEach {
      if (it is X509TrustManager) {
        manager = it
        return@forEach
      }
    }
    return manager
      ?: throw IllegalStateException("Unexpected default trust managers: ${Arrays.toString(tmFactory.trustManagers)}")
  }

}
