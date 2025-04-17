package com.android.visitormanagementsystem.ui.adminreports

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.SnackbarEvent
import com.android.visitormanagementsystem.binding.bind

class AdminReportsViewState   (
    downloadStatusVisibility : Boolean = false,
    initSnackbarEvent: SnackbarEvent = SnackbarEvent.NONE,
    initAdminReportsList: List<AdminReportsUiModel> = emptyList()
) : BaseObservable() {

    @get: Bindable
    var progressbarEvent by bind(BR.progressbarEvent, downloadStatusVisibility)

    @get:Bindable
    var snackbarEvent by bind(BR.snackbarEvent, initSnackbarEvent)

    @get: Bindable
    var initAdminReportsList by bind(BR.initAdminReportsList, initAdminReportsList)
}