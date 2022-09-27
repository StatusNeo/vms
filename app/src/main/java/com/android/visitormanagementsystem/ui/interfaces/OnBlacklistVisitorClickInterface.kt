package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.blacklistedvisitor.BlackListedVisitorUiModel

interface OnBlacklistVisitorClickInterface {
    fun openReportScreen(items: List<BlackListedVisitorUiModel>)
}