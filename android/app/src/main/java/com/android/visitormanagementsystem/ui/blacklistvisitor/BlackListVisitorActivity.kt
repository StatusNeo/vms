package com.android.visitormanagementsystem.ui.blacklistvisitor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.android.visitormanagementsystem.BlackListVisitorBinding
import com.android.visitormanagementsystem.R
import com.google.firebase.firestore.FieldValue
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.getCurrentDate
import com.android.visitormanagementsystem.utils.toast

class BlackListVisitorActivity : AppCompatActivity() {
    var blacklistViewState = ProgressBarViewState()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(BlackListVisitorBinding.inflate(layoutInflater).apply {
            viewState = blacklistViewState
            etName.requestFocus()
            etReason.requestFocus()
            etMobile.requestFocus()
            btnNext.setOnClickListener {
                if(etMobile.text?.isBlank() != false) {
                    etMobile.error = getString(R.string.error_visitor_mobile)
                } else if(etMobile.text!!.length != 10) {
                    etMobile.requestFocus()
                    etMobile.error = "Mobile number should be 10 digit's";
                } else if(!etMobile.text!!.matches(Constants.MOBILE_NUMBER_REGEX.toRegex())) {
                    etMobile.requestFocus()
                    etMobile.error = "Invalid Mobile Number";
                } else if(etName.text?.isBlank() != false) {
                    etName.requestFocus()
                    etName.error = getString(R.string.please_enter_visitor)
                } else if(etReason.text.isBlank()) {
                    etReason.requestFocus()
                    etReason.error = getString(R.string.please_enter_reason)
                } else {
                    addBlockedVisitor(
                        etName.text.toString(), etMobile.text.toString(), etReason.text.toString()
                    )
                }
            }
            backBtn.setOnClickListener {
                this@BlackListVisitorActivity.finish()
            }
        }.root)
    }

    private fun addBlockedVisitor(name: String, mobile: String, reason: String) {
        var isExisting = false
        blacklistViewState.progressbarEvent = true
        val myDB = FirebaseFirestore.getInstance().collection(Constants.BLACKLIST_VISITOR)
        val query1: com.google.firebase.firestore.Query =
            myDB.whereEqualTo(Constants.MOBILE_N0, mobile)
        query1.get().addOnSuccessListener { result ->
            for(document in result) {
                val resultMobileNo = document.data[Constants.MOBILE_N0].toString()
                isExisting = resultMobileNo == mobile
            }
            if(!isExisting) {
                saveNewBlacklistVisitor(name, mobile, reason, myDB)

            } else {
                blacklistViewState.progressbarEvent = false
                toast(R.string.user_already_blacklisted)
            }
        }.addOnFailureListener {
            blacklistViewState.progressbarEvent = false
            toast(R.string.msg_something_went)
        }
    }

    private fun saveNewBlacklistVisitor(
        name: String,
        mobile: String,
        reason: String,
        myDB: CollectionReference
    ) {
        myDB.add(
            mapOf(
                Constants.DATE to getCurrentDate(),
                Constants.NAME to name,
                Constants.MOBILE_N0 to mobile,
                Constants.REASON to reason,
                Constants.TIMESTAMP to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener {
            blacklistViewState.progressbarEvent = false
            this@BlackListVisitorActivity.finish()
            toast(R.string.msg_blocked)
        }.addOnFailureListener {
            blacklistViewState.progressbarEvent = false
            toast(R.string.msg_something_went)
        }
    }
}