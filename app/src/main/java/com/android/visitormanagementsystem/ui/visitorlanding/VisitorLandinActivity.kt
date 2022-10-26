package com.android.visitormanagementsystem.ui.visitorlanding

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.VisitorLandingBinding
import com.android.visitormanagementsystem.ui.adminpanel.AdminPanelActivity
import com.android.visitormanagementsystem.ui.adminreports.AdminReportsActivity
import com.android.visitormanagementsystem.ui.host.hostlogin.HostLoginViewModel
import com.android.visitormanagementsystem.ui.host.hostreports.HostReportsActivity
import com.android.visitormanagementsystem.ui.interfaces.OnVerifyVisitorInterface
import com.android.visitormanagementsystem.ui.registervisitor.RegisterVisitorActivity
import com.android.visitormanagementsystem.ui.visitorList.VisitorListActivity
import com.android.visitormanagementsystem.ui.visitorList.VisitorListUiModel
import com.android.visitormanagementsystem.ui.visitorphoto.VisitiorPhotoActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    lateinit var binding: VisitorLandingBinding
    var selectedPosition : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        hostLoginViewModel = ViewModelProvider(this)[HostLoginViewModel::class.java]
        setContentView(VisitorLandingBinding.inflate(layoutInflater).apply {
            binding = this
            viewStateProgress = progressViewState
            hostLoginViewModel.setVerifyVisitorInterface(this@VisitorLandingActivity)
            btnSendOtp.setOnClickListener {
                sendOTPClick()
            }

            otp1.afterTextChanged()
            otp2.afterTextChanged()
            otp3.afterTextChanged()
            otp4.afterTextChanged()
            otp5.afterTextChanged()
            otp6.afterTextChanged()

            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    progressViewState.progressbarEvent = false

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    Log.d(ContentValues.TAG, "onVerificationCompleted:$credential")
                    //  signInWithPhoneAuthCredential(credential,et_user_name.text.toString())
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressViewState.progressbarEvent = false
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    Log.d(ContentValues.TAG, "onCodeSent:$verificationId")
                    storedVerificationId = verificationId
                    resendToken = token

                    // Verify OTP view visible
                    etOtp.visibility = View.VISIBLE
                    fmLayoutVerifyBtn.visibility = View.VISIBLE
                    tv4Landing.visibility = View.VISIBLE
                    tvOtpTimer.visibility=View.VISIBLE

                    // time count down for 30 seconds,
                    // with 1 second as countDown interval
                    object : CountDownTimer(30000, 1000) {

                        // Callback function, fired on regular interval
                        override fun onTick(millisUntilFinished: Long) {
                            tvOtpTimer.text = "Resend OTP after: " + millisUntilFinished / 1000
                        }

                        // Callback function, fired
                        // when the time is up
                        override fun onFinish() {
                           // tvOtpTimer.setText("done!")
                            tvOtpTimer.visibility=View.GONE
                            tv3Landing.visibility = View.VISIBLE
                        }
                    }.start()

                    tv1Landing.setText( R.string.enter_your_verification_code)
                    tv2Landing.text = "We have sent verification code to \n $useMobileNo"
                    imageLanding.setImageResource( R.drawable.landing_enter_otp)
                    // Send OTP view invisible
                    tILMobile.visibility = View.GONE
                    fmLayoutSendOtp.visibility = View.GONE

                    toast(R.string.msg_otp_sent)
                }
            }

            btnVerifyOtp.setOnClickListener {
                var getOTP = otp1.text.toString() + otp2.text.toString() +otp3.text.toString() +otp4.text.toString() +otp5.text.toString() +otp6.text.toString()
                if(getOTP.length == 6){
                    // handle verification process
                    progressViewState.progressbarEvent = true
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, getOTP)
                    signInWithPhoneAuthCredential(credential, useMobileNo)
                }else{
                    toast(R.string.text_enter_otp)
                }



            /*
                   var otp = etOtp.text.toString().trim()
                if(otp.isBlank()) {
                    toast(R.string.text_enter_otp)
                } else {
                    progressViewState.progressbarEvent = true
                    val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)
                    signInWithPhoneAuthCredential(credential, useMobileNo)
                }*/
            }

            tv3Landing.setOnClickListener {
                selectedPosition = 0
                showKeyboard(binding.otp1)
                otp1.setText("")
                otp2.setText("")
                otp3.setText("")
                otp4.setText("")
                otp5.setText("")
                otp6.setText("")
                sendOTPClick()
                tv3Landing.visibility=View.GONE
            }

            tv4Landing.setOnClickListener {

                selectedPosition = 0
                showKeyboard(binding.otp1)
                otp1.setText("")
                otp2.setText("")
                otp3.setText("")
                otp4.setText("")
                otp5.setText("")
                otp6.setText("")

                // Verify OTP view invisible
                etOtp.visibility = View.GONE
                fmLayoutVerifyBtn.visibility = View.GONE
                tv3Landing.visibility = View.GONE
                tv4Landing.visibility = View.GONE
                useMobileNo = ""
                binding.etMobileNumber.text = null

                // Send OTP view visible
                tILMobile.visibility = View.VISIBLE
                fmLayoutSendOtp.visibility = View.VISIBLE
                tv1Landing.setText( "Enter your \nmobile number")
                tv2Landing.setText("Enter your 10 digit mobile \n number below")
                imageLanding.setImageResource( R.drawable.landing_login)
            }
        }.root)
    }

    override fun onBackPressed() {
        this@VisitorLandingActivity.finish()
    }

    private fun sendOTPClick(){

        useMobileNo = binding.etMobileNumber.text.toString()
        if(useMobileNo.isBlank()) {
            binding.etMobileNumber.requestFocus()
            binding.etMobileNumber.error = "Please enter mobile number"
        } else if(useMobileNo.length != 10) {
            binding.tILMobile.isErrorEnabled = false
            binding.etMobileNumber.error = "Mobile number should be 10 digit's"
        } else if(!useMobileNo.matches(Constants.MOBILE_NUMBER_REGEX.toRegex())) {
            binding.etMobileNumber.error = "Invalid Mobile Number"
        } else {
            progressViewState.progressbarEvent = true
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            startPhoneNumberVerification("+91$useMobileNo")
        }
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
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if(isEmployee) {
            when(empRole) {
                "Employee" -> {
                    val intent =
                        Intent(this@VisitorLandingActivity, HostReportsActivity::class.java)
                    intent.putExtra("mobile", useMobileNo)
                    startActivity(intent)
                    this@VisitorLandingActivity.finish()
                }
                "M" -> print("user is a Manager")
                "Admin" -> startActivity(
                    Intent(
                        this@VisitorLandingActivity,
                        AdminPanelActivity::class.java
                    )
                )
                "Security" -> startActivity(
                    Intent(
                        this@VisitorLandingActivity,
                        VisitorListActivity::class.java
                    )
                )
            }
            this@VisitorLandingActivity.finish()
        } else {

            val intent = Intent(this@VisitorLandingActivity, VisitiorPhotoActivity::class.java)
            intent.putExtra("mobile", useMobileNo)
            println("Mobile $useMobileNo")
            startActivity(intent)
            this@VisitorLandingActivity.finish()
        }
    }


    private fun EditText.afterTextChanged() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {

                if(editable.toString().length > 0){
                    if(selectedPosition == 0){
                        // select next edittext
                        selectedPosition = 1
                        showKeyboard(binding.otp2)

                    }else if(selectedPosition == 1){
                        // select next edittext
                        selectedPosition = 2
                        showKeyboard(binding.otp3)
                    }else if(selectedPosition == 2){

                        // select next edittext
                        selectedPosition = 3
                        showKeyboard(binding.otp4)
                    }else if(selectedPosition == 3){

                        // select next edittext
                        selectedPosition = 4
                        showKeyboard(binding.otp5)
                    }else if(selectedPosition == 4){

                        // select next edittext
                        selectedPosition = 5
                        showKeyboard(binding.otp6)
                    }else {

                        // verify btn red bg
                    }
                }
            }
        })
    }

    private fun showKeyboard(editText: EditText){
        editText.requestFocus()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_DEL){
            if(selectedPosition==5){

                // select previous edittext
                selectedPosition = 4
                showKeyboard(binding.otp5)
            } else if(selectedPosition==4){

                // select previous edittext
                selectedPosition = 3
                showKeyboard(binding.otp4)
            } else if(selectedPosition==3){

                // select previous edittext
                selectedPosition = 2
                showKeyboard(binding.otp3)
            } else if(selectedPosition==2){

                // select previous edittext
                selectedPosition = 1
                showKeyboard(binding.otp2)
            } else if(selectedPosition==1){

                // select previous edittext
                selectedPosition = 0
                showKeyboard(binding.otp1)
            }
            // verify btn brown bg
            return true
        }else{
            return super.onKeyUp(keyCode, event)
        }
    }
}