package com.android.visitormanagementsystem.ui.visitorotp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityVisitorsOtpFirebBinding
import com.android.visitormanagementsystem.ui.adapters.VisitorOTPFirbAdapter
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorOTPFirbClickInterface
import com.android.visitormanagementsystem.ui.registervisitor.RegisterVisitorActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.toast
import java.util.concurrent.TimeUnit

class VisitorsOTPFirbActivity: AppCompatActivity() {
    lateinit var viewModelOtp : VisitorsOTPFirbViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    private var phoneNumber: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        viewModelOtp = ViewModelProvider(this)[VisitorsOTPFirbViewModel::class.java]
        setContentView(ActivityVisitorsOtpFirebBinding.inflate(layoutInflater).apply {
            otpsViewStateProgress = viewModelOtp.otpListViewState
            viewModel = viewModelOtp
            viewState = viewModelOtp.viewState
            with(visitorOTPRecyclerView){
                adapter = VisitorOTPFirbAdapter(object : OnVisitorOTPFirbClickInterface {
                    override fun onOTPVerifyClick(uiModel: VisitorsOTPFirbUIModel, pos: String) {
                        if (pos.isBlank()) {
                            toast(R.string.text_enter_otp)
                        }else if (storedVerificationId==""){
                            toast(R.string.alert_resend_otp)

                        }else{
                            phoneNumber = uiModel.visitorMobileNo
                            val credential =
                                PhoneAuthProvider.getCredential(storedVerificationId!!, pos)
                            phoneNumber?.let { signInWithPhoneAuthCredential(credential, it) }
                        }

                    }
                    override fun onOTPResendClick(uiModel: VisitorsOTPFirbUIModel, pos: Int) {
                        phoneNumber=uiModel.visitorMobileNo
                        startPhoneNumberVerification("+91$phoneNumber")
                    }


                })
            }

            backBtn.setOnClickListener {
                this@VisitorsOTPFirbActivity.finish()
            }

            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(ContentValues.TAG, "onVerificationCompleted:$credential")
                    toast(R.string.text_otp_resent)
                    phoneNumber?.let { signInWithPhoneAuthCredential(credential, it) }
                }

                override fun onVerificationFailed(e: FirebaseException) {

                   // toast(R.string.msg_something_went)
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                    }
                    val toast: Toast= Toast.makeText(this@VisitorsOTPFirbActivity, e.message, Toast.LENGTH_SHORT)
                    toast.show()
                    // Show a message and update the UI
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(ContentValues.TAG, "onCodeSent:$verificationId")
                    storedVerificationId = verificationId
                    resendToken = token
                    storedVerificationId=verificationId
                    toast(R.string.text_otp_resent)


                }
            }

        }.root)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(10L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, mobile:String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    toast(R.string.text_otp_verified)
                    val db=Firebase.firestore

                    var query=db.collection(Constants.MANAGE_OTP).whereEqualTo(Constants.VISITOR_MOBILE,phoneNumber)
                        .get()
                    query.addOnSuccessListener {
                        for (document in it){
                            db.collection(Constants.MANAGE_OTP).document(document.id).delete()
                            val intent = Intent(this@VisitorsOTPFirbActivity, RegisterVisitorActivity::class.java)
                            intent.putExtra("mobile",phoneNumber)
                            startActivity(intent)
                            this@VisitorsOTPFirbActivity.finish()

                        }
                    } .addOnFailureListener { exception ->
                        Log.w("checkout error>>", "Error getting documents.", exception)
                    }



                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    toast(R.string.msg_otp_invalid)

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }



}