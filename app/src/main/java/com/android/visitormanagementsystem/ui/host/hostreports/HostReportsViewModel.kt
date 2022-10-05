package com.android.visitormanagementsystem.ui.host.hostreports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.visitormanagementsystem.ui.interfaces.OnReportDownloadInterface
import com.android.visitormanagementsystem.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class HostReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore
    var initReportList: ArrayList<HostReportUiModel> = ArrayList()
    private lateinit var onReportInterface: OnReportDownloadInterface

    fun initHostReport(selectedDate: String, hostMobileNo: String) {
        initReportList.clear()
        callVisitorListData(hostMobileNo, selectedDate)
    }

    private fun addListDataInUIModel(result: QuerySnapshot) {
        if(!result.isEmpty) {
            for(document in result) {
                val stamp = document.data[Constants.TIMESTAMP] as Timestamp
                val date = stamp.toDate()
                initReportList.add(
                    HostReportUiModel(
                        document.id,
                        document.data[Constants.VISITOR_NAME].toString(),
                        document.data[Constants.VISIT_DATE].toString(),
                        document.data[Constants.IN_TIME].toString(),
                        document.data[Constants.VISITOR_MOBILE].toString(),
                        document.data[Constants.NO_OF_PERSONS].toString(),
                        date
                    )
                )
            }
        }
        if(initReportList.isEmpty()) {
            onReportInterface.openReportScreen(
                initReportList
            )
        } else {
            initReportList.sortByDescending { it.timestamp }
            onReportInterface.openReportScreen(
                initReportList
            )
        }
    }

    private fun callVisitorListData(
        hostMobileNo: String,
        selectedDate: String,
    ) {
        val myDB = db.collection(Constants.VISITOR_LIST)
        val query: Query = myDB.whereEqualTo(Constants.HOST_MOBILE, hostMobileNo)
        query.whereEqualTo(Constants.VISIT_DATE, selectedDate).get()
            .addOnSuccessListener { result ->
                addListDataInUIModel(result)
            }.addOnFailureListener { exception ->
            Timber.tag("PlanVisit error>>").w(exception, "Error getting documents.")
        }
    }

    fun setReportInterface(onReportInterface: OnReportDownloadInterface) {
        this.onReportInterface = onReportInterface
    }
}