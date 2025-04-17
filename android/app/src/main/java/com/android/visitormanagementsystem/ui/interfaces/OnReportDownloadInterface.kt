package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.host.hostreports.HostReportUiModel

interface OnReportDownloadInterface {
    fun openReportScreen(items: List<HostReportUiModel>)
}