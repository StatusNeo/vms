package com.android.visitormanagementsystem.ui.visitorotp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState

class VisitorsOTPFirbViewModel (application: Application) : AndroidViewModel(application) {

    var viewState = VisitorsOTPFirbViewState()
    var initManageOTPListList: ArrayList<VisitorsOTPFirbUIModel> = ArrayList()
    private val db = Firebase.firestore
    var otpListViewState = ProgressBarViewState()

    init {

        initOtpList()
    }

    private fun initOtpList() {
        otpListViewState.progressbarEvent=true
        db.collection(Constants.MANAGE_OTP)
            .orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
            .get().addOnSuccessListener { result ->
                for (document in result) {
                    if (result.isEmpty) {
                        viewState.initList = emptyList()
                    } else {
                        initManageOTPListList.add(
                            VisitorsOTPFirbUIModel(
                                document.id.toString(),
                                document.data[Constants.VISITOR_MOBILE].toString(),
                            )
                        )
                    }
                }
                viewState.initList = initManageOTPListList
                otpListViewState.progressbarEvent=false



            }
            .addOnFailureListener { exception ->
                otpListViewState.progressbarEvent=false
                Log.w("users error>>", "Error getting documents.", exception)

            }


    }
}