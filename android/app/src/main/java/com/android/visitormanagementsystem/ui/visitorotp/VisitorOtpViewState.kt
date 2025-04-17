package com.android.visitormanagementsystem.ui.visitorotp

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.binding.bind
import com.android.visitormanagementsystem.BR

class VisitorOtpViewState     (
    initVisitorOTPList: List<VisitorOTPUiModel> = emptyList()
) : BaseObservable() {

    @get: Bindable
    var initList by bind(BR.initList, initVisitorOTPList)
}
