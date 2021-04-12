package org.markensic.emqx.base

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.markensic.sdk.global.ActivityStack
import com.markensic.sdk.global.App
import com.markensic.sdk.global.LibStackContext

class EmqxApp : Application(), LibStackContext {

  override val activityStack: ActivityStack = ActivityStack()

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    MultiDex.install(this)
    App.initApplication(this)
  }

  override fun onCreate() {
    super.onCreate()
  }
}