package org.markensic.emqx.test

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import org.markensic.emqx.R
import org.markensic.emqx.base.BaseActivity
import org.markensic.emqx.databinding.ActivityTestCertManagerBinding
import org.markensic.emqx.viewmodels.TestViewModel

class TestCertManagerActivity : BaseActivity() {

  lateinit var binding: ActivityTestCertManagerBinding
  val viewModel: TestViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_test_cert_manager)
    binding.vm = viewModel
    requestPermissions()

    viewModel.mainTest()
  }
}
