package com.android.visitormanagementsystem.ui.blacklistedvisitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.android.visitormanagementsystem.ui.interfaces.OnBlacklistVisitorClickInterface
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.Constants.DATE_FORMAT
import java.text.SimpleDateFormat

class BlackListedVisitorViewModel(application: Application) : AndroidViewModel(application) {

    var initVisitorList: ArrayList<BlackListedVisitorUiModel> = ArrayList()
    private lateinit var onReportInterface: OnBlacklistVisitorClickInterface

    fun initVisitorList(fromDate: String, toDate: String) {
        initVisitorList.clear()
        val myDB = FirebaseFirestore.getInstance().collection(Constants.BLACKLIST_VISITOR)
        val query1: Query = myDB.orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
        query1.get().addOnSuccessListener { result ->

            if(result.isEmpty) {
                onReportInterface.openReportScreen(
                    initVisitorList
                )
            } else {
                for(document in result) {
                    if(checkDateBetween(
                            document.data[Constants.DATE].toString(),
                            fromDate,
                            toDate
                        )
                    ) {
                        initVisitorList.add(
                            BlackListedVisitorUiModel(
                                document.id,
                                document.data[Constants.NAME].toString(),
                                document.data[Constants.MOBILE_N0].toString(),
                                document.data[Constants.DATE].toString()
                            )
                        )
                    }
                }
                if(initVisitorList.isNotEmpty()) {
                    onReportInterface.openReportScreen(
                        initVisitorList
                    )
                }else{
                    onReportInterface.openReportScreen(
                        initVisitorList
                    )
                }
            }
        }.addOnFailureListener {
            onReportInterface.openReportScreen(
                initVisitorList
            )
        }
    }

    fun checkDateBetween(readableDateString: String, fromDate: String, endDate: String): Boolean {
        val dateFormat = SimpleDateFormat(DATE_FORMAT)
        val readableDate = dateFormat.parse(readableDateString)
        val todayDate = dateFormat.parse(fromDate)
        val lastDate = dateFormat.parse(endDate)
        return (!readableDate.before(todayDate) && !readableDate.after(lastDate))
    }

    fun setVisitorInterface(onReportInterface: OnBlacklistVisitorClickInterface) {
        this.onReportInterface = onReportInterface
    }
}