package com.android.visitormanagementsystem.ui.gethost

import java.util.*

data class HostProfileUiModel(
    var id: String? = null,
    var hostName: String? = null,
    var hostMobileNo: String? = null,
    var hostEmail: String? = null,
    var designation: String? = null,
    var timestamp: Date? = null,
    var role: String? = null
)
