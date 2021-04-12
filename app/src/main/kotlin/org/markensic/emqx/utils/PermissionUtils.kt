package org.markensic.emqx.utils

import android.app.Activity
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object PermissionUtils {
  val PERMISSION_CODE = 0x777

  fun hasPermissions(c: Context, vararg permissions: String) = EasyPermissions.hasPermissions(c, *permissions)

  fun requestPermissions(act: Activity, rejectMsg: String, vararg permissions: String) {
    if (!hasPermissions(act, *permissions)) {
      EasyPermissions.requestPermissions(act, rejectMsg, PERMISSION_CODE, *permissions)
    }
  }
}