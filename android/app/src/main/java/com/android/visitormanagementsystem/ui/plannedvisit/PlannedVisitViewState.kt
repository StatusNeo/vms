package com.android.visitormanagementsystem.ui.plannedvisit

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.android.visitormanagementsystem.BR
import com.android.visitormanagementsystem.binding.bind

class PlannedVisitViewState(
    initPlannedVisitList: List<PlannedVisitUiModel> = emptyList()
) : BaseObservable() {

    @get: Bindable
    var initPlannedList by bind(BR.initPlannedList, initPlannedVisitList)
}