package org.markensic.emqx.client.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import org.markensic.emqx.R
import org.markensic.emqx.viewmodels.AddSubjectTopicViewModel
import com.markensic.sdk.ui.dp
import org.markensic.emqx.databinding.DialogAddSubjectBinding

class SubjectTopicDialog private constructor(val c: Context) :
  Dialog(c) {
  private val binding: DialogAddSubjectBinding =
    DataBindingUtil.inflate(layoutInflater, R.layout.dialog_add_subject, null, false)
  private val viewModel = AddSubjectTopicViewModel()

  init {
    binding.viewmodel = viewModel
    setContentView(binding.root)
    setCancelable(true)

    window?.apply {
      setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      val lp = WindowManager.LayoutParams();
      lp.copyFrom(attributes);
      lp.width = 300.dp.toInt()
      lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
      attributes = lp
    }
  }

  class Builder(context: Context) {
    val dialog = SubjectTopicDialog(context)

    fun setConfirmListener(l: (topic: String, qos: String) -> Unit): Builder {
      return this.also {
        dialog.binding.confirm.setOnClickListener {
          l(dialog.viewModel.subjectTopic, dialog.viewModel.qos)
          dialog.dismiss()
        }
      }
    }

    fun show() {
      dialog.show()
    }
  }
}