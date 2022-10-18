package com.android.visitormanagementsystem.ui.addhostprofile

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.android.visitormanagementsystem.AddHostBinding
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast

class AddHostActivity : AppCompatActivity() {
    var addHostViewState = ProgressBarViewState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(AddHostBinding.inflate(layoutInflater).apply {
            viewState = addHostViewState
          //  etUserName.requestFocus()
            btnSubmit.setOnClickListener {
                 if(etHostName.text.isNullOrBlank()) {
                    etHostName.requestFocus()
                    etHostName.error = "Please enter Host Full name"
                } else if(etHostMobile.text.isNullOrBlank()) {
                    etHostMobile.requestFocus()
                    etHostMobile.error = "Please enter Host mobile no."
                } else if(etHostMobile.text.toString().isNotEmpty() && etHostMobile.text?.length != 10) {
                    etHostMobile.requestFocus()
                    etHostMobile.error = "Mobile number should be 10 digit's"
                } else if(!etHostMobile.text.toString().matches(Constants.MOBILE_NUMBER_REGEX.toRegex())) {
                    etHostMobile.requestFocus()
                    etHostMobile.error = "Invalid Mobile Number"
                }  else if(etEmailId.text.toString().isNotEmpty() && !etEmailId.text.toString().matches(Constants.EMAIL_REGEX.toRegex())) {
                    etEmailId.error = "Please enter valid Email"
                } else {
                    addNewHost(
                        etHostName.text.toString(),
                        etHostMobile.text.toString(),
                        etEmailId.text.toString() ?: "",
                        etHostDesignation.text.toString() ?: ""
                    )
                }
            }
        }.root)
    }

    private fun addNewHost(
        fullName: String,
        mobile: String,
        email: String,
        designation: String
    ) {
        var isMobileExisting =  false
        var isUserNameExisting = false
        addHostViewState.progressbarEvent = true
        val myDB = FirebaseFirestore.getInstance().collection(Constants.EMPLOYEE_LIST)
        myDB .get().addOnSuccessListener { result ->

            result.query
            for(document in result) {
                val resultMobile = document.data[Constants.HOST_MOBILE].toString()
                if(!isMobileExisting){
                    isMobileExisting = resultMobile == mobile
                }
            }
                if(!isMobileExisting) {
                    val host =    mapOf(
                        Constants.HOST_NAME to fullName,
                        Constants.HOST_MOBILE to mobile,
                        Constants.EMAIL to email,
                        Constants.DESIGNATION to designation,
                        Constants.TIMESTAMP to FieldValue.serverTimestamp(),
                        Constants.EMP_ROLE to "Employee"
                    )

                        saveNewHost(host,myDB)
            } else {
                addHostViewState.progressbarEvent = false
                    if(isMobileExisting) toast(R.string.host_already_exist)
                    else if(isMobileExisting)  toast(R.string.host_mobile_already_exist)
            }
        }.addOnFailureListener {
            addHostViewState.progressbarEvent = false
            toast(R.string.msg_something_went)
        }
    }

    private fun saveNewHost(
        host: Map<String, Any>,
        myDB: CollectionReference,
    ) {
        myDB.add(host).addOnSuccessListener {
            addHostViewState.progressbarEvent = false
            toast(R.string.toast_host_saved)
            this@AddHostActivity.finish()

        }.addOnFailureListener {
            addHostViewState.progressbarEvent = false
            toast(R.string.msg_something_went)
        }
    }
}