package com.android.visitormanagementsystem.ui.host.planvisit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.getCurrentDate
import timber.log.Timber
import java.text.SimpleDateFormat

class PlanVisitViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore

    fun getAllPlanVisit(mobileNo : String) {
        db.collection(Constants.PLANNED_VISIT)
            .whereEqualTo(Constants.HOST_MOBILE,mobileNo)
            .get().addOnSuccessListener { result ->
                for(document in result) {
                    if(result.isEmpty) {
                        Timber.tag(" PlanVisit>>").w("No data found")
                    } else {
                        Timber.tag(" PlanVisit>>").w(document.data[Constants.VISIT_DATE].toString() + "  docID  " + document.id.toString())
                        if(checkDateLessThanToday(document.data[Constants.VISIT_DATE].toString())) {
                            val docRef = db.collection(Constants.PLANNED_VISIT).document( document.id)
                            docRef.delete()
                        }
                    }
                }
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
}