package com.android.visitormanagementsystem.utils

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.bind

class ProgressBarViewState(downloadStatusVisibility : Boolean = false) : BaseObservable()  {

    @get: Bindable
    var progressbarEvent by bind(BR.progressbarEvent, downloadStatusVisibility)

}