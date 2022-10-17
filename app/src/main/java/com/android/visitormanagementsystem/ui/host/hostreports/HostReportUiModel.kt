package com.android.visitormanagementsystem.ui.host.hostreports

import java.util.*

data class HostReportUiModel(
    var id: String? = null,
    var visitorName: String? = null,
    var visitDate: String? = null,
    var visittime: String? = null,
    var visitorMobileNo: String? = null,
    var timestamp: Date? = null
)
