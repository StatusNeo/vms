package com.android.visitormanagementsystem.ui.host.visitorreports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.ui.interfaces.OnReportDownloadInterface
import com.android.visitormanagementsystem.utils.Constants
import timber.log.Timber

class VisitorReportsViewModel (application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore
    var initReportList: ArrayList<VisitorReportsUIModel> = ArrayList()
    private lateinit var onReportInterface : OnReportDownloadInterface
    fun initHostReport(selectedDate: String, hostMobileNo: String) {
        initReportList.clear()
        callVisitorsData(hostMobileNo,selectedDate)

    }

    private fun callVisitorsData(
        hostMobileNo: String,
        selectedDate: String
    ) {
        db.collection(Constants.VISITOR_LIST).whereEqualTo(Constants.HOST_MOBILE, hostMobileNo)
            .whereEqualTo(Constants.VISIT_DATE,selectedDate).orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING).get().addOnSuccessListener { result ->
                addListDataInUIModel(result)
            }.addOnFailureListener { exception ->
                Timber.tag("HostReport error>>").w(exception, "Error getting documents.")
            }
    }

    private fun addListDataInUIModel(result: QuerySnapshot) {
        for(document in result) {
            if(!result.isEmpty) {
                initReportList.add(
                    VisitorReportsUIModel(
                        document.id,
                        document.data[Constants.VISITOR_NAME].toString(),
                        document.data[Constants.VISIT_DATE].toString(),
                        document.data[Constants.IN_TIME].toString(),
                        document.data[Constants.VISITOR_MOBILE].toString(),
                        document.data[Constants.NO_OF_PERSONS].toString()
                    )
                )
            }
        }
//        if(initReportList.isEmpty()) {
//            onReportInterface.openReportScreen(
//                initReportList
//            )
//        } else {
//            onReportInterface.openReportScreen(
//                initReportList
//            )
//        }
    }
}