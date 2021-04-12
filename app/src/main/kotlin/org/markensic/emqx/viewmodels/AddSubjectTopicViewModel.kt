package org.markensic.emqx.viewmodels

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import org.markensic.emqx.BR

class AddSubjectTopicViewModel : BaseObservable() {

  @get:Bindable
  var subjectTopic = "testtopic/#"
    set(value) {
      if (value != field) {
        field = value
        notifyPropertyChanged(BR.subjectTopic)
      }
    }

  @get:Bindable
  var qos = "1"
    set(value) {
      if (value != field) {
        field = value
        notifyPropertyChanged(BR.qos)
      }
    }

}