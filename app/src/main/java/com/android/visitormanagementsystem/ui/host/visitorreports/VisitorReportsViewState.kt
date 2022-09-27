package com.android.visitormanagementsystem.ui.host.visitorreports

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.SnackbarEvent
import com.android.visitormanagementsystem.binding.bind

class VisitorReportsViewState (
    hvisitorReportUiList: List<VisitorReportsUIModel> = emptyList(),
    initSnackbarEvent: SnackbarEvent = SnackbarEvent.NONE
) : BaseObservable() {

    @get: Bindable
    var initVisitorReport by bind(BR.initHostReport, hvisitorReportUiList)

    @get:Bindable
    var snackbarEvent by bind(BR.snackbarEvent, initSnackbarEvent)
}
