package com.android.visitormanagementsystem.ui.verifyvisitor

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.bind

class VerifyVisitorViewState (

        prevoiusVisitsVisibility : Boolean = false,
        ): BaseObservable() {

        @get: Bindable
        var progressbarEvent by bind(BR.progressbarEvent, prevoiusVisitsVisibility)
}