package com.android.visitormanagementsystem.ui.host.hostlogin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.ui.interfaces.OnVerifyVisitorInterface
import com.android.visitormanagementsystem.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

class HostLoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private lateinit var verifyVisitorInterface: OnVerifyVisitorInterface

    fun getHostDetails(userMobile: String) {

        val myDB = FirebaseFirestore.getInstance().collection(Constants.EMPLOYEE_LIST)
        val query1: Query = myDB.whereEqualTo(Constants.HOST_MOBILE, userMobile)
        query1.get().addOnSuccessListener { result ->
            if(result.isEmpty) {
                verifyVisitorInterface.checkVisitorType(false, "")
                Timber.tag(" getHostDetails>>").w("No data found")
            } else {
                for(document in result) {
                    verifyVisitorInterface.checkVisitorType(
                        true, document.data[Constants.EMP_ROLE].toString()
                    )
                    Timber.tag(" getHostDetails>>")
                        .w(document.data[Constants.HOST_MOBILE].toString())
                }
            }
        }.addOnFailureListener { exception ->
            verifyVisitorInterface.checkVisitorType(false, "")
            Timber.tag("HostDetails error>>").w(exception, "Error getting documents.")
        }
    }

    fun setVerifyVisitorInterface(onVerifyVisitorInterface: OnVerifyVisitorInterface) {
        this.verifyVisitorInterface = onVerifyVisitorInterface
    }
}