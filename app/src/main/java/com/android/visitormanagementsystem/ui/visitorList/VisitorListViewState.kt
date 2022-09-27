package com.android.visitormanagementsystem.ui.visitorList

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.binding.bind
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.SnackbarEvent

class VisitorListViewState     (downloadStatusVisibility : Boolean = false,
                                initSnackbarEvent: SnackbarEvent = SnackbarEvent.NONE,
                                initHomeworkReportsList: List<VisitorListUiModel> = emptyList()
) : BaseObservable() {

    @get: Bindable
    var initReportsList by bind(BR.initReportsList, initHomeworkReportsList)
    @get: Bindable
    var progressbarEvent by bind(BR.progressbarEvent, downloadStatusVisibility)

    @get:Bindable
    var snackbarEvent by bind(BR.snackbarEvent, initSnackbarEvent)

}
