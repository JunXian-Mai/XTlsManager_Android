package org.markensic.emqx.client.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.markensic.emqx.R
import org.markensic.emqx.base.BaseActivity
import org.markensic.emqx.base.constant.CodeConstant
import org.markensic.emqx.base.constant.KeyConstant
import org.markensic.emqx.viewmodels.ConnectViewModel
import com.markensic.sdk.global.sdkLogi
import org.markensic.emqx.databinding.ActivityClientConnectBinding

class ClientConnectActivity : BaseActivity() {

  lateinit var binding: ActivityClientConnectBinding
  val viewModel: ConnectViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_client_connect)
    binding.viewmodel = viewModel
    requestPermissions()

    observeFiled()
    setEvent()
  }

  private fun setEvent() {
    binding.enableTls.setOnClickListener {
      if (binding.enableTls.isChecked) {
        binding.groupTls.visibility = View.VISIBLE
      } else {
        binding.groupTls.visibility = View.GONE
      }
    }

    binding.verifyGroup.setOnCheckedChangeListener { group, checkedId ->
      when (checkedId) {
        binding.oneWayCertification.id -> {
          viewModel.setTwoWayCertification(false)
        }
        binding.twoWayCertification.id -> {
          viewModel.setTwoWayCertification(true)
        }
      }
    }

    binding.ca.setOnClickListener {
      viewModel.selectFile(this, CodeConstant.SELECT_ROOTCA_CODE)
    }

    binding.clientKey.setOnClickListener {
      viewModel.selectFile(this, CodeConstant.SELECT_CLIENT_KEY_CODE)
    }

    binding.clientPem.setOnClickListener {
      viewModel.selectFile(this, CodeConstant.SELECT_CLIENT_PEM_CODE)
    }

    binding.connect.setOnClickListener {
      val intent = Intent(this, ClientMessageActivity::class.java)
      val bean = viewModel.createConnectBean()
      intent.putExtra(KeyConstant.CONNECT_BEAN, bean)
      startActivity(intent)
    }
  }

  private fun observeFiled() {
    viewModel.name.observe(this, Observer<String> {
      if (binding.name.text.toString() != it) {
        binding.name.setText(it)
      }
    })

    viewModel.clientId.observe(this, Observer<String> {
      if (binding.clientId.text.toString() != it) {
        binding.clientId.setText(it)
      }
    })

    viewModel.address.observe(this, Observer<String> {
      if (binding.address.text.toString() != it) {
        binding.address.setText(it)
      }
    })

    viewModel.user.observe(this, Observer<String> {
      if (binding.user.text.toString() != it) {
        binding.user.setText(it)
      }
    })

    viewModel.password.observe(this, Observer<String> {
      if (binding.password.text.toString() != it) {
        binding.password.setText(it)
      }
    })

    viewModel.keepAlive.observe(this, Observer<String> {
      if (binding.keepAlive.text.toString() != it) {
        binding.keepAlive.setText(it)
      }
    })

    viewModel.cleanSession.observe(this, Observer<Boolean> {
      if (binding.cleanSession.isChecked != it) {
        binding.cleanSession.isChecked = it
      }
    })

    viewModel.enableTls.observe(this, Observer<Boolean> {
      if (binding.enableTls.isChecked != it) {
        binding.enableTls.isChecked = it
      }
    })

    viewModel.oneWayCertification.observe(this, Observer<Boolean> {
      if (binding.oneWayCertification.isChecked != it) {
        binding.oneWayCertification.isChecked = it
      }
    })

    viewModel.twoWayCertification.observe(this, Observer<Boolean> {
      if (binding.twoWayCertification.isChecked != it) {
        binding.twoWayCertification.isChecked = it
      }
    })

    viewModel.ca.observe(this, Observer<String> {
      if (binding.ca.text.toString() != it) {
        binding.ca.setText(it)
      }
    })

    viewModel.clientKey.observe(this, Observer<String> {
      if (binding.clientKey.text.toString() != it) {
        binding.clientKey.setText(it)
      }
    })

    viewModel.clientPem.observe(this, Observer<String> {
      if (binding.clientPem.text.toString() != it) {
        binding.clientPem.setText(it)
      }
    })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    sdkLogi("requestCode: $requestCode resultCode: $resultCode")
    if (resultCode == RESULT_OK) {
      data?.data?.let {
        viewModel.setFilePath(it, requestCode)
      }
    }
  }

}