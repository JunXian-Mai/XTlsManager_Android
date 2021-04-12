package org.markensic.emqx.base

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.markensic.emqx.R
import org.markensic.emqx.utils.PermissionUtils
import com.gyf.immersionbar.ktx.immersionBar

open class BaseActivity : AppCompatActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    immersionBar {
      transparentStatusBar()
      statusBarDarkFont(true)
      keyboardEnable(true)
      navigationBarColor(R.color.material_grey_50)
    }
  }

  fun requestPermissions() {
    PermissionUtils.requestPermissions(
      this,
      "App无法正常使用，请允许权限请求",
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_PHONE_STATE
//            , Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )
  }
}