package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.visitorotp.VisitorOTPUiModel

interface OnVisitorOTPClickInterface {
    fun onOTPVerifyClick(uiModel: VisitorOTPUiModel, pos : Int)
    fun onOTPResendClick(uiModel: VisitorOTPUiModel, pos : Int)
}