package com.android.visitormanagementsystem.ui.adminreports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.android.visitormanagementsystem.ui.interfaces.OnAdminReportInterface
import com.android.visitormanagementsystem.utils.Constants

class AdminReportsViewModel(application: Application) : AndroidViewModel(application) {

    var viewState = AdminReportsViewState()
    var initVisitorList: ArrayList<AdminReportsUiModel> = ArrayList()
    private lateinit var onReportInterface: OnAdminReportInterface


    fun initVisitorList(selectedDate: String) {
        initVisitorList.clear()
        val adminDB = FirebaseFirestore.getInstance()
        adminDB.collection(Constants.VISITOR_LIST).whereEqualTo(Constants.VISIT_DATE, selectedDate)
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
                        initVisitorList.add(
                            AdminReportsUiModel(
                                document.id,
                                document.data[Constants.VISITOR_NAME].toString(),
                                document.data[Constants.VISITOR_MOBILE].toString(),
                                document.data[Constants.HOST_NAME].toString(),
                                document.data[Constants.BATCH_NO].toString(),
                                visitorImage,
                                document.data[Constants.IN_TIME].toString(),
                                document.data[Constants.OUT_TIME].toString()

                            )
                        )
                    }

                    onReportInterface.openReportScreen(initVisitorList)
                }
            }.addOnFailureListener {
                onReportInterface.openReportScreen(initVisitorList)
            }
    }

    fun setReportInterface(onReportInterface: OnAdminReportInterface) {
        this.onReportInterface = onReportInterface
    }
}