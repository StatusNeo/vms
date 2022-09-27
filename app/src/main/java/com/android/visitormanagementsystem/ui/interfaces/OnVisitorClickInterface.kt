package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.visitorList.VisitorListUiModel

interface OnVisitorClickInterface {
    fun onVisitorClick(uiModel: VisitorListUiModel, pos : Int)
}