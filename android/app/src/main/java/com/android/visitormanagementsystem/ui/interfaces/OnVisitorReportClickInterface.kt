package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.adminreports.AdminReportsUiModel

interface OnVisitorReportClickInterface {
    fun onVisitorClick(uiModel: AdminReportsUiModel, pos : Int)
}