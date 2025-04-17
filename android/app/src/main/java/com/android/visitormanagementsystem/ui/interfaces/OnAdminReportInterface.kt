package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.adminreports.AdminReportsUiModel

interface OnAdminReportInterface {
    fun openReportScreen(items: List<AdminReportsUiModel>)
}