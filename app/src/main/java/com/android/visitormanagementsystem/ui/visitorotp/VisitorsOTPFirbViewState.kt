package com.android.visitormanagementsystem.ui.visitorotp

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.bind

class VisitorsOTPFirbViewState  (
    initVisitorOTPList: List<VisitorsOTPFirbUIModel> = emptyList()
) : BaseObservable() {

    @get: Bindable
    var initList by bind(BR.initList, initVisitorOTPList)
}