package com.android.visitormanagementsystem.ui.blacklistedvisitor

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.SnackbarEvent
import com.android.visitormanagementsystem.binding.bind

class BlackListedVisitorViewState (
    downloadStatusVisibility : Boolean = false,
    initBlockedList: List<BlackListedVisitorUiModel> = emptyList(),
    initSnackbarEvent: SnackbarEvent = SnackbarEvent.NONE
) : BaseObservable() {

    @get: Bindable
    var progressbarEvent by bind(BR.progressbarEvent, downloadStatusVisibility)

    @get: Bindable
    var initBlockedList by bind(BR.initBlockedList, initBlockedList)

    @get:Bindable
    var snackbarEvent by bind(BR.snackbarEvent, initSnackbarEvent)
}
