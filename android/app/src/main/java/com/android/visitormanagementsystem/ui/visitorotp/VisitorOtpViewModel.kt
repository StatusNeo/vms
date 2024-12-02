package com.android.visitormanagementsystem.ui.visitorotp

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class VisitorOtpViewModel(application: Application) : AndroidViewModel(application) {

    var viewState = VisitorOtpViewState()
    init {
        initOtpList()
    }

    private fun initOtpList() {
        var vi = VisitorOTPUiModel(201,"9810897628",2903)
        var v2 = VisitorOTPUiModel(201,"9828743391",9330)
        var v5 = VisitorOTPUiModel(201,"8328703304",4209)
        var v6 = VisitorOTPUiModel(201,"7829474739",2903)
        var v3 = VisitorOTPUiModel(201,"7989939339",1103)
        var initVisitorList: ArrayList<VisitorOTPUiModel> = ArrayList()

        initVisitorList.add(vi)
        initVisitorList.add(v2)
        initVisitorList.add(v3)
        initVisitorList.add(v5)
        initVisitorList.add(v6)
        viewState.initList = initVisitorList
    }
}