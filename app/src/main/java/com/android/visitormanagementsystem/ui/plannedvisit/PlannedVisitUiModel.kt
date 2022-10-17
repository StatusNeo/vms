package com.android.visitormanagementsystem.ui.plannedvisit

data class PlannedVisitUiModel(
    var id: String? = null,
    var visitDate: String? = null,
    var visittime: String? = null,
    var hostName: String? = null,
    var hostMobileNo: String? = null,
    var purpose: String? = null,
    var visitorName: String? = null,
    var visitorImage: String? = null
)

data class PlannedVisitVisitorDataModel(
    var id: String? = null,
    var visitorName: String? = null,
    var visitorMobileNo: String? = null,
    var visitorImage: String? = null
)
