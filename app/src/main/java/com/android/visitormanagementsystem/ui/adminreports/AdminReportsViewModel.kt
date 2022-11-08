package com.android.visitormanagementsystem.ui.adminreports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.visitormanagementsystem.ui.interfaces.OnAdminReportInterface
import com.android.visitormanagementsystem.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminReportsViewModel(application: Application) : AndroidViewModel(application) {
    var initVisitorList: ArrayList<AdminReportsUiModel> = ArrayList()
    private lateinit var onReportInterface: OnAdminReportInterface

    fun initVisitorList(selectedDate: String) {
        initVisitorList.clear()
        val adminDB = FirebaseFirestore.getInstance().collection(Constants.VISITOR_LIST)
        adminDB.whereEqualTo(Constants.VISIT_DATE, selectedDate)
            .get().addOnSuccessListener { result ->
                if(result.isEmpty) {
                    onReportInterface.openReportScreen(
                        initVisitorList
                    )
                } else {
                    for(document in result) {
                        if(document.data[Constants.OUT_TIME]==null){
                            document.data[Constants.OUT_TIME]="NA"
                        }
                        var visitorImage:String=""
                        visitorImage = if (document.data["visitorImage"].toString().isNullOrBlank() ||document.data["visitorImage"].toString().isNullOrEmpty() ){
                            "https://opt.toiimg.com/recuperator/img/toi/m-69257289/69257289.jpg"

                        }else{
                            document.data["visitorImage"].toString()
                        }

                        val stamp = document.data[Constants.TIMESTAMP] as Timestamp
                        val date = stamp.toDate()

                        initVisitorList.add(
                            AdminReportsUiModel(
                                document.id,
                                document.data[Constants.VISITOR_NAME].toString(),
                                document.data[Constants.VISITOR_MOBILE].toString(),
                                document.data[Constants.HOST_NAME].toString(),
                                document.data[Constants.BATCH_NO].toString(),
                                visitorImage,
                                document.data[Constants.VISIT_DATE].toString(),
                                document.data[Constants.IN_TIME].toString(),
                                document.data[Constants.OUT_TIME].toString(),
                                date
                            )
                        )
                    }
                    initVisitorList.sortByDescending { it.timestamp  }
                    onReportInterface.openReportScreen(initVisitorList)
                }
            }.addOnFailureListener {
                onReportInterface.openReportScreen(initVisitorList)
            }
    }

    fun searchByName(name: String){
        println("Visitor Name is : $name")
        initVisitorList.clear()
        val myDB = FirebaseFirestore.getInstance().collection(Constants.VISITOR_LIST)
        val query1: Query = myDB.orderBy(Constants.VISITOR_NAME).startAt(name)
        query1.get()
            .addOnSuccessListener { result ->
                if(result.isEmpty) {
                    onReportInterface.openReportScreen(
                        initVisitorList
                    )
                } else {
                    for(document in result) {
                        if(document.data[Constants.OUT_TIME] == null) {
                            document.data[Constants.OUT_TIME] = "NA"
                        }
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
                        var visitorName = document.data[Constants.VISITOR_NAME].toString()
                        if(name in visitorName) {
                            println("VISITOR_NAME " + document.data[Constants.VISITOR_NAME].toString())
                            initVisitorList.add(
                                AdminReportsUiModel(
                                    document.id,
                                    document.data[Constants.VISITOR_NAME].toString(),
                                    document.data[Constants.VISITOR_MOBILE].toString(),
                                    document.data[Constants.HOST_NAME].toString(),
                                    document.data[Constants.BATCH_NO].toString(),
                                    visitorImage,
                                    document.data[Constants.VISIT_DATE].toString(),
                                    document.data[Constants.IN_TIME].toString(),
                                    document.data[Constants.OUT_TIME].toString(),
                                    date
                                )
                            )
                        }
                    }
                    initVisitorList.sortByDescending { it.timestamp  }
                    onReportInterface.openReportScreen(initVisitorList)
                    println("List size" + initVisitorList.size)
                }
            }.addOnFailureListener {
                onReportInterface.openReportScreen(initVisitorList)
            }

    }

    fun setReportInterface(onReportInterface: OnAdminReportInterface) {
        this.onReportInterface = onReportInterface
    }
}