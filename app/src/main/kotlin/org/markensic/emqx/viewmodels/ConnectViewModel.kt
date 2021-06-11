package org.markensic.emqx.viewmodels

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.markensic.emqx.base.constant.CodeConstant
import org.markensic.emqx.base.plugin.getRandomString
import org.markensic.emqx.models.ConnectBean
import com.markensic.sdk.utils.FileUtils
import org.markensic.emqx.cert.impl.XTlsHostnameConfigImpl
import org.markensic.xtls.constant.ASSETS_PRE

class ConnectViewModel : ViewModel() {

  val name = MutableLiveData("android_${getRandomString(6)}")
  val clientId = MutableLiveData("android_${getRandomString(6)}")
  val address = MutableLiveData("ssl://192.168.36.4:8883")
  val user = MutableLiveData("")
  val password = MutableLiveData("")
  val keepAlive = MutableLiveData("60")
  val cleanSession = MutableLiveData(true)
  val enableTls = MutableLiveData(true)
  val oneWayCertification = MutableLiveData(false)
  val twoWayCertification = MutableLiveData(true)

  private val defaultCA = "${ASSETS_PRE}certs/testssl/all-ca-my.crt"
  private val defaultPem = "${ASSETS_PRE}certs/testssl/clientmy.crt"
  private val defaultKey = "${ASSETS_PRE}certs/testssl/client-key.pem"

  val ca = MutableLiveData(defaultCA)
  val clientPem = MutableLiveData(defaultPem)
  val clientKey = MutableLiveData(defaultKey)


  fun selectFile(act: Activity, requestCode: Int) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "*/*"
    act.startActivityForResult(intent, requestCode)
  }

  fun setTwoWayCertification(isCheck: Boolean) {
    oneWayCertification.value = !isCheck
    twoWayCertification.value = isCheck
  }

  fun setFilePath(uri: Uri, requestCode: Int) {
    FileUtils.uriCastFile(uri)?.let {
      when (requestCode) {
        CodeConstant.SELECT_ROOTCA_CODE -> {
          ca.value = it.absolutePath
        }

        CodeConstant.SELECT_CLIENT_KEY_CODE -> {
          clientKey.value = it.absolutePath
        }

        CodeConstant.SELECT_CLIENT_PEM_CODE -> {
          clientPem.value = it.absolutePath
        }
      }
    }
  }

  fun createConnectBean(): ConnectBean {
    val keepalive = if (keepAlive.get().isBlank()) {
      60
    } else {
      keepAlive.get().toInt()
    }

    return ConnectBean(
      name.get(),
      clientId.get(),
      address.get(),
      user.get(),
      password.get(),
      keepalive,
      cleanSession.get(),
      enableTls.get(),
      oneWayCertification.get(),
      twoWayCertification.get(),
      ca.get(),
      clientPem.get(),
      clientKey.get()
    )
  }

  fun MutableLiveData<String>.get() = this.value ?: ""
  fun MutableLiveData<Boolean>.get() = this.value ?: false
}