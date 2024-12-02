package com.android.visitormanagementsystem.ui.visitorList

import java.util.*

data class VisitorListUiModel(
    var id: String? = null,
    var visitorName: String? = null,
    var visitorEmail: String? = null,
    var visitorMobileNo: String? = null,
    var visitorImage: String? = null,
    var visitDate: String? = null,
    var inTime: String? = null,
    var batchNo: String? = null,
    var hostName: String? = null,
    var hostMobileNo: String? = null,
    var purpose: String? = null,
    var address: String? = null,
    var gender: String? = null,
    var outTime: String? = null,
    var timestamp: Date? = null
    )
