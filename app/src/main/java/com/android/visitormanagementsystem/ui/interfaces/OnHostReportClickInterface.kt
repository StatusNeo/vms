package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.host.hostreports.HostReportUiModel


interface OnHostReportClickInterface {
    fun onVisitorClick(uiModel: HostReportUiModel, pos : Int)
}