package com.android.visitormanagementsystem.ui.visitorList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.binding.SnackbarEvent
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorListInterface
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.getCurrentDate
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VisitorListViewModel(application: Application) : AndroidViewModel(application) {
    var visitorListViewState = VisitorListViewState()
    private val db = Firebase.firestore
    var initVisitorList: ArrayList<VisitorListUiModel> = ArrayList()
    var viewState = VisitorListViewState()
    private lateinit var onReportInterface: OnVisitorListInterface

    init {
        initVisitorList()
    }

    private fun initVisitorList(){
        viewState.progressbarEvent = true
        val myDB = FirebaseFirestore.getInstance().collection(Constants.VISITOR_LIST)
        var chainedQuery1: Query =
            myDB.whereEqualTo(Constants.VISIT_DATE, getCurrentDate()).whereEqualTo("outTime", "NA")
        chainedQuery1.get().addOnSuccessListener { result ->
            viewState.progressbarEvent = false
            if(result.isEmpty) {
                onReportInterface.openVisitorsListScreen(
                    initVisitorList
                )
                viewState.initReportsList = emptyList()
                viewState.snackbarEvent = SnackbarEvent(R.string.msg_no_data)
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
                    initVisitorList.add(
                        VisitorListUiModel(
                            document.id.toString(),
                            document.data["visitorName"].toString(),
                            document.data["visitorEmail"].toString(),
                            document.data["visitorMobileNo"].toString(),
                            visitorImage,
                            document.data["visitDate"].toString(),
                            document.data["inTime"].toString(),
                            document.data["BadgeNumber"].toString(),
                            document.data["hostName"].toString(),
                            document.data["hostMobileNo"].toString(),
                            document.data["purpose"].toString(),
                            document.data["address"].toString(),
                            document.data["gender"].toString(),
                            document.data["outTime"].toString(),
                            date
                        )
                    )
                }

                initVisitorList.sortByDescending { it.timestamp  }
                visitorListViewState.initReportsList = initVisitorList
                onReportInterface.openVisitorsListScreen(
                    initVisitorList
                )
            }
        }
            .addOnFailureListener { exception ->
                viewState.progressbarEvent = false
                viewState.initReportsList = emptyList()
                viewState.snackbarEvent = SnackbarEvent(R.string.msg_no_data)
                onReportInterface.openVisitorsListScreen(
                    initVisitorList
                )
            }
    }
    fun setReportInterface(onReportInterface: OnVisitorListInterface) {
        this.onReportInterface = onReportInterface
    }
}