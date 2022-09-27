package com.android.visitormanagementsystem.ui.host.hostreports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.ui.interfaces.OnReportDownloadInterface
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.getCurrentDate
import timber.log.Timber
import java.text.SimpleDateFormat

class HostReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore
    var initReportList: ArrayList<HostReportUiModel> = ArrayList()
    private lateinit var onReportInterface : OnReportDownloadInterface

    fun initHostReport(selectedDate: String, hostMobileNo: String) {
        initReportList.clear()

        callVisitorListData(hostMobileNo,selectedDate)

      /*  if(checkDateLessThanToday(selectedDate)) {

            callVisitorListData(hostMobileNo,selectedDate)
        } else {
            callPlannedVisitData(hostMobileNo,selectedDate)
        }*/
    }

    private fun callPlannedVisitData(
        hostMobileNo: String,
        selectedDate: String
    ) {
        db.collection(Constants.PLANNED_VISIT).whereEqualTo(Constants.HOST_MOBILE, hostMobileNo)
            .whereEqualTo(Constants.VISIT_DATE,selectedDate).get().addOnSuccessListener { result ->
                addListDataInUIModel(result)
            }.addOnFailureListener { exception ->
                Timber.tag("HostReport error>>").w(exception, "Error getting documents.")
            }
    }

    private fun addListDataInUIModel(result: QuerySnapshot) {
        for(document in result) {
            if(!result.isEmpty) {
                initReportList.add(
                    HostReportUiModel(
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
        if(initReportList.isEmpty()) {
            onReportInterface.openReportScreen(
                initReportList
            )
        } else {
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
       val query : Query = myDB.whereEqualTo(Constants.HOST_MOBILE, hostMobileNo)
        query.whereEqualTo(Constants.VISIT_DATE,selectedDate).get().addOnSuccessListener { result ->
                addListDataInUIModel(result)
            }.addOnFailureListener { exception ->
                Timber.tag("PlanVisit error>>").w(exception, "Error getting documents.")
            }
    }

    fun checkDateLessThanToday(readableDateString: String): Boolean {
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
        val readableDate = dateFormat.parse(readableDateString)
        val todayDate = dateFormat.parse(getCurrentDate())
        return (readableDate.before(todayDate))
    }

    fun  setReportInterface(onReportInterface : OnReportDownloadInterface){
        this.onReportInterface = onReportInterface
    }
}