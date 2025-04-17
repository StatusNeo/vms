package com.android.visitormanagementsystem.ui.plannedvisit

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.android.visitormanagementsystem.ui.interfaces.VisitorDataInterface
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.getCurrentDate
import java.text.SimpleDateFormat

class PlannedVisitViewModel (application: Application) : AndroidViewModel(application) {
    var visitorProfileModel = PlannedVisitVisitorDataModel()
    var plannedVisitViewState = ProgressBarViewState()
    var viewState = PlannedVisitViewState()
    var initVisitorList: ArrayList<PlannedVisitUiModel> = ArrayList()
    var visitorName: String? =null
    private lateinit var onReportInterface:VisitorDataInterface
//    init {
//        checkPreviousVisits("")
//    }
      fun checkPreviousVisits( mobile:String){
        plannedVisitViewState.progressbarEvent = true
        val myDB = FirebaseFirestore.getInstance().collection(Constants.VISITOR_LIST)
        val query1: Query = myDB.whereEqualTo(Constants.VISITOR_MOBILE,mobile).orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
        query1.get().addOnSuccessListener { result ->
            plannedVisitViewState.progressbarEvent = false
            if(result.isEmpty) {
                Log.d("in>>>>","empty")

            } else {
                Log.d("in>>>>","non-empty")
                for (document in result) {
                    if (checkDateBetween(document.data[Constants.VISIT_DATE].toString(), getCurrentDate()
                        )) {
                        Log.d("host>>>>",document.data[Constants.VISITOR_NAME].toString())
                            visitorName=document.data[Constants.VISITOR_NAME].toString()

                        initVisitorList.add(PlannedVisitUiModel(
                           document.id,
                           document.data[Constants.VISIT_DATE].toString(),
                                   document.data[Constants.IN_TIME].toString(),
                           document.data[Constants.HOST_NAME].toString(),
                           document.data[Constants.HOST_MOBILE].toString(),
                           document.data[Constants.PURPOSE].toString(),
                            document.data[Constants.VISITOR_NAME].toString(),
                            document.data[Constants.VISITOR_IMAGE].toString(),

                            ))
                    }
                }
                viewState.initPlannedList = initVisitorList
                visitorProfileModel = PlannedVisitVisitorDataModel(initVisitorList[0].id,
                    initVisitorList[0].visitorName,
                    mobile,
                    initVisitorList[0].visitorImage,
                )
                onReportInterface.onData(visitorProfileModel)

            }
        }.addOnFailureListener {
            plannedVisitViewState.progressbarEvent = false
            Log.d("error", it.message.toString());
        }
    }

    fun checkPlannedVisits( mobile:String){
        plannedVisitViewState.progressbarEvent = true
        val myDB = FirebaseFirestore.getInstance().collection(Constants.PLANNED_VISIT)
        val query1: Query = myDB.whereEqualTo(Constants.VISITOR_MOBILE,mobile).orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
        query1.get().addOnSuccessListener { result ->
            plannedVisitViewState.progressbarEvent = false
            if(result.isEmpty) {
                Log.d("in>>>>","empty")

            } else {
                Log.d("in>>>>","non-empty")
                for (document in result) {
                    if (checkPlannedDate(document.data[Constants.VISIT_DATE].toString(), getCurrentDate()
                        )) {
                        Log.d("host>>>>",document.data[Constants.VISITOR_NAME].toString())
                        visitorName=document.data[Constants.VISITOR_NAME].toString()

                        initVisitorList.add(PlannedVisitUiModel(
                            document.id,
                            document.data[Constants.VISIT_DATE].toString(),
                            document.data[Constants.IN_TIME].toString(),
                            document.data[Constants.HOST_NAME].toString(),
                            document.data[Constants.HOST_MOBILE].toString(),
                            document.data[Constants.PURPOSE].toString(),
                            document.data[Constants.VISITOR_NAME].toString(),
                            ))
                    }
                }
                viewState.initPlannedList = initVisitorList
                visitorProfileModel = PlannedVisitVisitorDataModel(initVisitorList[0].id,
                    initVisitorList[0].visitorName,
                    mobile,
                )
                onReportInterface.onData(visitorProfileModel)


            }
        }.addOnFailureListener {
            plannedVisitViewState.progressbarEvent = false
            Log.d("error", it.message.toString());
        }
    }
    fun  setReportInterface(onReportInterface : VisitorDataInterface){
        this.onReportInterface = onReportInterface
    }
    fun setPreviousVisitorProfile(model : PlannedVisitVisitorDataModel){
        visitorProfileModel = PlannedVisitVisitorDataModel(
            model.id,
            model.visitorName,
            model.visitorMobileNo,
            model.visitorImage
        )
    }

    fun setVisitorProfile(mobile : String){
        visitorProfileModel = PlannedVisitVisitorDataModel(
            "101",
            "Ritesh",
            mobile,
            "https://www.pust.ac.bd/includes/images/teachers/Tofail%20Ahmed.jpg"
        )
    }
    private fun checkDateBetween(readableDateString: String, fromDate: String): Boolean {
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
        val readableDate = dateFormat.parse(readableDateString)
        val todayDate = dateFormat.parse(fromDate)
        return (readableDate.before(todayDate) )
    }
    private fun checkPlannedDate(readableDateString: String, fromDate: String): Boolean {
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
        val readableDate = dateFormat.parse(readableDateString)
        val todayDate = dateFormat.parse(fromDate)
        return (readableDate.after(todayDate)|| readableDate.equals(todayDate))
    }

}
