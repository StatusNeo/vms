package com.android.visitormanagementsystem.ui.host.hostreports

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.SnackbarEvent
import com.android.visitormanagementsystem.binding.bind

class HostReportsViewState(
    hostReportUiList: List<HostReportUiModel> = emptyList(),
    initSnackbarEvent: SnackbarEvent = SnackbarEvent.NONE
) : BaseObservable() {

    @get: Bindable
    var initHostReport by bind(BR.initHostReport, hostReportUiList)

    @get:Bindable
    var snackbarEvent by bind(BR.snackbarEvent, initSnackbarEvent)
}
