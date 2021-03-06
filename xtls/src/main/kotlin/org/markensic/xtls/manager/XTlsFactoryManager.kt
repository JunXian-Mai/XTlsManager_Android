package org.markensic.xtls.manager

import android.util.Base64
import com.markensic.sdk.utils.AssetsUtils
import org.markensic.xtls.constant.ASSETS_PRE
import org.markensic.xtls.constant.SYSTEM_CERT_DIR
import java.io.*
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

object XTlsFactoryManager {

  private val caFactory = CertificateFactory.getInstance("X.509")

  /**
   * 获取Android System 中默认的证书信任库，信任的根证列表
   * 获取指定目录的文件导出信任根证列表
   * @return System 信任根证列表
   */
  fun getSystemTrustedCerts(): Array<X509Certificate> {
    val dir = File(SYSTEM_CERT_DIR)
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
      if (it.startsWith(ASSETS_PRE)) {
        val assetsName = it.replace(ASSETS_PRE, "")
        AssetsUtils.open(name = assetsName) { ci ->
          getCaCertificatesInStream(ci)
        }
      } else {
        getCaCertificate(it)
      }
    }.toTypedArray()
  }

  /**
   * 根据信任的证书路径获取信任的证书
   * @param caCertPath 信任的证书路径
   * @return 指定路径下的证书列表
   */
  fun getCaCertificates(caCertPath: String): Array<X509Certificate> {
    return if (caCertPath.startsWith(ASSETS_PRE)) {
      val assetsName = caCertPath.replace(ASSETS_PRE, "")
      AssetsUtils.open(name = assetsName) {
        arrayOf(
          getCaCertificatesInStream(it)
        )
      }
    } else {
      arrayOf(
        getCaCertificate(caCertPath)
      )
    }
  }

  /**
   * 根据信任的证书路径获取信任的证书
   * @param caCertPath 信任的证书路径
   * @return 指定路径下的证书
   */
  fun getCaCertificate(caCertPath: String): X509Certificate {
    return if (caCertPath.startsWith(ASSETS_PRE)) {
      val assetsName = caCertPath.replace(ASSETS_PRE, "")
      AssetsUtils.open(name = assetsName) {
        getCaCertificatesInStream(it)
      }
    } else {
      FileInputStream(caCertPath).use {
        getCaCertificatesInStream(it)
      }
    }
  }

  /**
   * 根据信任的证书内容列表获取信任的证书列表
   * @param caCertContents 信任的证书内容
   * @return 指定的证书列表
   */
  fun convertCaCertificates(caCertContents: Array<String>): Array<X509Certificate> {
    return caCertContents.map { content ->
      convertCaCertificate(content)
    }.toTypedArray()
  }

  /**
   * 根据信任的证书内容列表获取信任的证书列表
   * @param caCertContent 信任的证书内容
   * @return 指定的证书列表
   */
  fun convertCaCertificates(caCertContent: String): Array<X509Certificate> {
    return arrayOf(
      convertCaCertificate(caCertContent)
    )
  }

  /**
   * 根据信任的证书内容获取信任的证书
   * @param caCertContent 信任的证书内容
   * @return 指定的证书
   */
  fun convertCaCertificate(caCertContent: String): X509Certificate {
    return ByteArrayInputStream(caCertContent.toByteArray(Charsets.UTF_8)).use {
      getCaCertificatesInStream(it)
    }
  }

  /**
   * 根据信任的证书输入流获取信任的证书
   * @param ci 信任的证书输入流
   * @return 指定路径下的证书
   */
  fun getCaCertificatesInStream(ci: InputStream): X509Certificate {
    return caFactory.generateCertificate(ci) as X509Certificate
  }

  /**
   * 根据密钥文件路径，获取密钥文件中的密钥
   * @param certKeyPath 密钥文件路径
   * @return 密钥文件中的密钥
   */
  fun getCertPrivateKey(certKeyPath: String): PrivateKey {
    val builder = StringBuilder()
    return if (certKeyPath.startsWith(ASSETS_PRE)) {
      val assetsName = certKeyPath.replace(ASSETS_PRE, "")
      AssetsUtils.open(name = assetsName) {
        getCertPrivateKeyInStream(it)
      }
    } else {
      FileInputStream(certKeyPath).use {
        getCertPrivateKeyInStream(it)
      }
    }
  }

  /**
   * 根据密钥文件内容，获取密钥文件中的密钥
   * @param certKeyContent 密钥文件内容
   * @return 密钥文件中的密钥
   */
  fun convertCertPrivateKey(certKeyContent: String): PrivateKey {
    val keyIns = ByteArrayInputStream(certKeyContent.toByteArray(Charsets.UTF_8))
    return keyIns.use {
      getCertPrivateKeyInStream(it) as RSAPrivateKey
    }
  }

  /**
   * 根据密钥文件输入流，获取密钥文件中的密钥
   * @param keyIns 密钥文件输入流
   * @return 密钥文件中的密钥
   */
  fun getCertPrivateKeyInStream(keyIns: InputStream): PrivateKey {
    val builder = StringBuilder()
    val br = BufferedReader(InputStreamReader(keyIns))
    br.useLines {
      it.forEach { line ->
        if (line[0] != '-') {
          builder.append(line)
          builder.append('\r')
        }
      }
    }
    val buffer = Base64.decode(builder.toString(), Base64.NO_WRAP)
    val keySpec = PKCS8EncodedKeySpec(buffer)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
  }

  /**
   * 根据客户端证书及密钥路径，生成对应密钥的密钥管理器
   * @param clientCertPath 客户端证书路径
   * @param clientKeyPath 客户端密钥路径
   * @param password 密钥密码
   * @return 密钥管理器
   */
  fun getKeyManagerFactory(
    clientCertPath: String,
    clientKeyPath: String,
    password: String = ""
  ): KeyManagerFactory {
     return if (clientCertPath.startsWith(ASSETS_PRE) && clientKeyPath.startsWith(ASSETS_PRE)) {
      val certAssetsName = clientCertPath.replace(ASSETS_PRE, "")
      val keyAssetsName = clientKeyPath.replace(ASSETS_PRE, "")
      AssetsUtils.open(name = certAssetsName) { ci ->
        AssetsUtils.open(name = keyAssetsName) { ki ->
          getKeyManagerFactoryInStream(
            ci,
            ki,
            password
          )
        }
      }
    } else {
      val clientCertIns = FileInputStream(clientCertPath)
      val clientKeyIns = FileInputStream(clientKeyPath)
      clientCertIns.use { ci ->
        clientKeyIns.use { ki ->
          getKeyManagerFactoryInStream(
            ci,
            ki,
            password
          )
        }
      }
    }
  }

  /**
   * 根据客户端证书及密钥内容，生成对应密钥的密钥管理器
   * @param clientCertContent 客户端证书内容
   * @param clientKeyContent 客户端密钥内容
   * @param password 密钥密码
   * @return 密钥管理器
   */
  fun convertKeyManagerFactory(
    clientCertContent: String,
    clientKeyContent: String,
    password: String = ""
  ): KeyManagerFactory {
    val clientCertIns = ByteArrayInputStream(clientCertContent.toByteArray(Charsets.UTF_8))
    val clientKeyIns = ByteArrayInputStream(clientKeyContent.toByteArray(Charsets.UTF_8))
    return clientCertIns.use { ci ->
      clientKeyIns.use { ki ->
        getKeyManagerFactoryInStream(
          ci,
          ki,
          password
        )
      }
    }
  }

  /**
   * 根据客户端证书及密钥内容，生成对应密钥的密钥管理器
   * @param clientCertIns 客户端证书输入流
   * @param clientKeyIns 客户端密钥输入流
   * @param password 密钥密码
   * @return 密钥管理器
   */
  fun getKeyManagerFactoryInStream(
    clientCertIns: InputStream,
    clientKeyIns: InputStream,
    password: String = ""
  ): KeyManagerFactory {
    return KeyManagerFactory.getInstance("PKIX").apply {
      val clientCert: X509Certificate = caFactory.generateCertificate(clientCertIns) as X509Certificate
      val clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
      clientKeyStore.load(null, null)
      clientKeyStore.setCertificateEntry("certificate", clientCert)
      clientKeyStore.setKeyEntry(
        "private-key",
        getCertPrivateKeyInStream(clientKeyIns),
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
