package com.android.visitormanagementsystem.ui.interfaces

import com.android.visitormanagementsystem.ui.adminpanel.GetAllHostUiModel

interface OnHostListInterface {
    fun getHostList(items: List<GetAllHostUiModel>)
}