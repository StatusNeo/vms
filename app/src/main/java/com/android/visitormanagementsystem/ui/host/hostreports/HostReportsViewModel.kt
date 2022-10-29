package com.android.visitormanagementsystem.ui.host.hostreports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.visitormanagementsystem.ui.adminreports.AdminReportsUiModel
import com.android.visitormanagementsystem.ui.interfaces.OnReportDownloadInterface
import com.android.visitormanagementsystem.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
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

    fun searchByName(name: String,  hostMobileNo: String){
        println("Visitor Name is : $name")
        initReportList.clear()
        val myDB = FirebaseFirestore.getInstance().collection(Constants.VISITOR_LIST)
        val query1: Query = myDB.whereEqualTo(Constants.HOST_MOBILE, hostMobileNo)
        query1.get()
            .addOnSuccessListener { result ->

                addListDataInUIModel(result, name)
            }.addOnFailureListener {
                onReportInterface.openReportScreen(
                    initReportList
                )
            }
    }

    private fun addListDataInUIModel(result: QuerySnapshot, nameSearched : String) {
        if(!result.isEmpty) {

            if(nameSearched.isNotEmpty()) {
                for(document in result) {
                    var visitorName = document.data[Constants.VISITOR_NAME].toString()
                        val compareName =  visitorName.contains(nameSearched, ignoreCase = true)  /*visitorName.trim().equals(nameSearched.trim())*/
                        if(compareName){
                        var visitorImage: String = ""
                        visitorImage = if(document.data["visitorImage"].toString()
                                .isNullOrBlank() || document.data["visitorImage"].toString()
                                .isNullOrEmpty()
                        ) {
                            "https://opt.toiimg.com/recuperator/img/toi/m-69257289/69257289.jpg"
                        } else {
                            document.data["visitorImage"].toString()
                        }

                        val stamp = document.data[Constants.TIMESTAMP] as Timestamp
                        val date = stamp.toDate()
                        initReportList.add(
                            HostReportUiModel(
                                document.id,
                                document.data[Constants.VISITOR_NAME].toString(),
                                document.data[Constants.VISIT_DATE].toString(),
                                document.data[Constants.IN_TIME].toString(),
                                document.data[Constants.VISITOR_MOBILE].toString(),
                                date,
                                document.data[Constants.HOST_NAME].toString(),
                                document.data[Constants.BATCH_NO].toString(),
                                visitorImage,
                            )
                        )
                    }
                }

            } else {
                for(document in result) {
                    var visitorImage: String = ""
                    visitorImage = if(document.data["visitorImage"].toString()
                            .isNullOrBlank() || document.data["visitorImage"].toString()
                            .isNullOrEmpty()
                    ) {
                        "https://opt.toiimg.com/recuperator/img/toi/m-69257289/69257289.jpg"
                    } else {
                        document.data["visitorImage"].toString()
                    }

                    val stamp = document.data[Constants.TIMESTAMP] as Timestamp
                    val date = stamp.toDate()
                    initReportList.add(
                        HostReportUiModel(
                            document.id,
                            document.data[Constants.VISITOR_NAME].toString(),
                            document.data[Constants.VISIT_DATE].toString(),
                            document.data[Constants.IN_TIME].toString(),
                            document.data[Constants.VISITOR_MOBILE].toString(),
                            date,
                            document.data[Constants.HOST_NAME].toString(),
                            document.data[Constants.BATCH_NO].toString(),
                            visitorImage,
                        )
                    )
                }
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
                addListDataInUIModel(result, "")
            }.addOnFailureListener { exception ->
                onReportInterface.openReportScreen(
                    initReportList
                )
            Timber.tag("PlanVisit error>>").w(exception, "Error getting documents.")
        }
    }

    fun setReportInterface(onReportInterface: OnReportDownloadInterface) {
        this.onReportInterface = onReportInterface
    }
}