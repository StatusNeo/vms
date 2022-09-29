package com.android.visitormanagementsystem.ui.visitorlanding

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.visitormanagementsystem.R
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.VisitorLandingBinding
import com.android.visitormanagementsystem.ui.addhostprofile.AddHostActivity
import com.android.visitormanagementsystem.ui.adminpanel.AdminPanelActivity
import com.android.visitormanagementsystem.ui.adminreports.AdminReportsActivity
import com.android.visitormanagementsystem.ui.host.hostlogin.HostLoginViewModel
import com.android.visitormanagementsystem.ui.host.hostreports.HostReportsActivity
import com.android.visitormanagementsystem.ui.interfaces.OnVerifyVisitorInterface
import com.android.visitormanagementsystem.ui.registervisitor.RegisterVisitorActivity
import com.android.visitormanagementsystem.ui.visitorList.VisitorListActivity
import com.android.visitormanagementsystem.ui.visitorList.VisitorListUiModel
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import java.util.concurrent.TimeUnit

class VisitorLandingActivity : AppCompatActivity(), OnVerifyVisitorInterface {
    private lateinit var auth: FirebaseAuth
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    var initVisitorList: ArrayList<VisitorListUiModel> = ArrayList()
    var progressViewState = ProgressBarViewState()
    var useMobileNo: String = ""
    private lateinit var hostLoginViewModel: HostLoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        hostLoginViewModel = ViewModelProvider(this)[HostLoginViewModel::class.java]
        setContentView(VisitorLandingBinding.inflate(layoutInflater).apply {
            viewStateProgress = progressViewState
            hostLoginViewModel.setVerifyVisitorInterface(this@VisitorLandingActivity)
            btnSendOtp.setOnClickListener {
                useMobileNo = etMobileNumber.text.toString()
                if(useMobileNo.isBlank()) {
                    etMobileNumber.requestFocus()
                    etMobileNumber.error = "Please enter mobile number"
                } else if(useMobileNo.length != 10) {
                    tILMobile.isErrorEnabled = false
                    etMobileNumber.error = "Mobile number should be 10 digit's"
                } else if(!useMobileNo.matches(Constants.MOBILE_NUMBER_REGEX.toRegex())) {
                    etMobileNumber.error = "Invalid Mobile Number"
                } else {
                    progressViewState.progressbarEvent = true
                    startPhoneNumberVerification("+91$useMobileNo")
                }
            }

            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    progressViewState.progressbarEvent = false

                    Log.d(ContentValues.TAG, "onVerificationCompleted:$credential")
                    //  signInWithPhoneAuthCredential(credential,et_user_name.text.toString())
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressViewState.progressbarEvent = false
                    //  etOtp.visibility= View.VISIBLE
                    Log.w(ContentValues.TAG, "onVerificationFailed", e)
                    if(e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                    } else if(e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                    }
                    val toast: Toast =
                        Toast.makeText(this@VisitorLandingActivity, e.message, Toast.LENGTH_SHORT)
                    toast.show()
                }

                override fun onCodeSent(
                    verificationId: String, token: PhoneAuthProvider.ForceResendingToken
                ) {
                    progressViewState.progressbarEvent = false
                    Log.d(ContentValues.TAG, "onCodeSent:$verificationId")
                    storedVerificationId = verificationId
                    resendToken = token
                    etOtp.visibility = View.VISIBLE
                    fmLayoutVerifyBtn.visibility = View.VISIBLE
                    toast(R.string.msg_otp_sent)
                }
            }

            btnVerifyOtp.setOnClickListener {
                var otp = etOtp.text.toString().trim()
                if(otp.isBlank()) {
                    toast(R.string.text_enter_otp)
                } else {
                    progressViewState.progressbarEvent = true
                    val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)
                    signInWithPhoneAuthCredential(credential, useMobileNo)
                }
            }
        }.root)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(10L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, mobile: String) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    hostLoginViewModel.getHostDetails(mobile)
                } else {
                    progressViewState.progressbarEvent = false
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    toast(R.string.msg_otp_invalid)
                    if(task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
        }
    }

    override fun checkVisitorType(isEmployee: Boolean, empRole: String) {
        progressViewState.progressbarEvent = false
        if(isEmployee) {
            when(empRole) {
                "E" -> startActivity(
                    Intent(
                        this@VisitorLandingActivity,
                        HostReportsActivity::class.java
                    )
                )
                "M" -> print("user is a Manager")
                "A" -> startActivity(
                    Intent(
                        this@VisitorLandingActivity,
                        AdminPanelActivity::class.java
                    )
                )
                "S" -> print("user is a Security")
            }
            this@VisitorLandingActivity.finish()
        } else {
            val intent = Intent(this@VisitorLandingActivity, RegisterVisitorActivity::class.java)
            intent.putExtra("mobile", useMobileNo)
            startActivity(intent)
            this@VisitorLandingActivity.finish()
        }
    }
}