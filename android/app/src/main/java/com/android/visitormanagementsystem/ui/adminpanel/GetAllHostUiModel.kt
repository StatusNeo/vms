package com.android.visitormanagementsystem.ui.adminpanel

import java.io.Serializable
import java.util.*

data class GetAllHostUiModel(
    var id: String? = null,
    var hostName: String? = null,
    var hostMobileNo: String? = null,
    var hostEmail: String? = null,
    var designation: String? = null,
    var role: String? = null,
    var hostImage : String? = null
) : Serializable
