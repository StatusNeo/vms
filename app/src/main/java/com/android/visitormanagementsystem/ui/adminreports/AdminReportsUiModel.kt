package com.android.visitormanagementsystem.ui.adminreports

import java.util.*

data class AdminReportsUiModel (
    var id: String? = null,
    var visitorName: String? = null,
    var visitorMobileNo: String? = null,
    var hostName: String? = null,
    var batchNo: String? = null,
    var visitorImage: String? = null,
    var visitDate: String? = null,
    var inTime: String? = null,
    var outTime: String? = null,
    var timestamp: Date? = null
)