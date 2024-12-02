package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.visitorotp.VisitorsOTPFirbUIModel

interface OnVisitorOTPFirbClickInterface {
    fun onOTPVerifyClick(uiModel: VisitorsOTPFirbUIModel, pos : String)
    fun onOTPResendClick(uiModel: VisitorsOTPFirbUIModel, pos : Int)
}