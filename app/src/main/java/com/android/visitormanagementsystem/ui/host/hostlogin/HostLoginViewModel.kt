package com.android.visitormanagementsystem.ui.host.hostlogin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.ui.interfaces.OnVerifyVisitorInterface
import com.android.visitormanagementsystem.utils.Constants
import timber.log.Timber

class HostLoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private lateinit var verifyVisitorInterface : OnVerifyVisitorInterface

    fun getHostDetails(userMobile: String) {
        db.collection(Constants.EMPLOYEE_LIST)
            .whereEqualTo(Constants.HOST_MOBILE, userMobile)
            .get().addOnSuccessListener { result ->
                for(document in result) {
                    if(result.isEmpty) {
                        verifyVisitorInterface.checkVisitorType(false,"")
                        Timber.tag(" getHostDetails>>").w("No data found")
                    } else {
                        verifyVisitorInterface.checkVisitorType(true,document.data[Constants.EMP_ROLE].toString())
                        Timber.tag(" getHostDetails>>").w(document.data[Constants.HOST_MOBILE].toString())
                    }
                }
            }.addOnFailureListener { exception ->
                verifyVisitorInterface.checkVisitorType(false,"")
                Timber.tag("HostDetails error>>").w(exception, "Error getting documents.")
            }
    }

    fun  setVerifyVisitorInterface(onVerifyVisitorInterface : OnVerifyVisitorInterface){
        this.verifyVisitorInterface = onVerifyVisitorInterface
    }
}