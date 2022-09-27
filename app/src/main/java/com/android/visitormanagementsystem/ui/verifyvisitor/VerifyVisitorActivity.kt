package com.android.visitormanagementsystem.ui.verifyvisitor

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.android.visitormanagementsystem.R
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.visitormanagementsystem.VerifyVisitorBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.ui.plannedvisit.PlannedVisitActivity
import com.android.visitormanagementsystem.ui.registervisitor.RegisterVisitorActivity
import com.android.visitormanagementsystem.ui.visitorList.VisitorListUiModel
import com.android.visitormanagementsystem.ui.visitorotp.VisitorsOTPFirbActivity
import com.android.visitormanagementsystem.utils.*
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class VerifyVisitorActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    private var isBlackListed:Boolean=false;
    var initVisitorList: ArrayList<VisitorListUiModel> = ArrayList()
    var progressViewState = ProgressBarViewState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(VerifyVisitorBinding.inflate(layoutInflater).apply {
            viewStateProgress=progressViewState

        }.root)
        auth = Firebase.auth
        var et_user_name = findViewById<EditText>(R.id.et_mobile_number)
        var btn_next = findViewById<Button>(R.id.btn_next)
        var  btn_send_otp= findViewById<Button>(R.id.btn_send_otp)
//        var tv_otp_text=findViewById<TextView>(R.id.tv_otp_text)
//        var tv_otp_value=findViewById<TextView>(R.id.tv_otp_value)
        var  btn_verify_otp= findViewById<Button>(R.id.btn_verify_otp)
        var tILMobile=findViewById<TextInputLayout>(R.id.tILMobile)
        var tv_previous_visit=findViewById<TextView>(R.id.tv_previous_visit)
        var tv_planned_visit=findViewById<TextView>(R.id.tv_planned_visit)
        var backBtn=findViewById<ImageView>(R.id.backBtn)
        var etOtp=findViewById<EditText>(R.id.et_otp)
        var fm_layout=findViewById<FrameLayout>(R.id.fm_layout)
        var fm_layout_verify_btn=findViewById<FrameLayout>(R.id.fm_layout_verify_btn)
        var pg_bar_send_otp=findViewById<ProgressBar>(R.id.pg_bar_send_otp)
        var tv_manage_otp=findViewById<TextView>(R.id.tv_manage_otp)
        var tv_override_otp=findViewById<TextView>(R.id.tv_override_otp)

        tv_override_otp.setOnClickListener {
            val intent = Intent(this@VerifyVisitorActivity, RegisterVisitorActivity::class.java)
            intent.putExtra("mobile",et_user_name.text.toString())
            startActivity(intent)
            this@VerifyVisitorActivity.finish()
        }

        et_user_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkBlackListedMobile(s.toString())

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if(count==10){
                            checkBlackListedMobile(s.toString())

                        }
            }
        })
        btn_next.setOnClickListener {

            tv_planned_visit.alpha = 0.5f
            tv_previous_visit.alpha = 0.5f
            var useMobileNo =  et_user_name.text.toString()
            if(useMobileNo.isBlank()){
                et_user_name.requestFocus()
                et_user_name.error = "Please enter mobile number"

            }else if (useMobileNo.length!=10 ){
                tILMobile.isErrorEnabled=false
                et_user_name.error = "Mobile number should be 10 digit's";
            }else if(!useMobileNo.matches(Constants.MOBILE_NUMBER_REGEX.toRegex()) ){
                et_user_name.error = "Invalid Mobile Number";
            }
            else if(isBlackListed){
                showToast(R.string.msg_blacklisted)
                fm_layout.visibility= View.GONE
                tv_previous_visit.visibility= View.GONE
                tv_planned_visit.visibility= View.GONE
            }
            else{
                progressViewState.progressbarEvent=true
                markButtonDisable(btn_next)
                checkPreviousVisits(tv_previous_visit,useMobileNo)
                checkPlannedVisits(tv_planned_visit,useMobileNo)
                showToast(R.string.msg_verified)
                fm_layout.visibility= View.VISIBLE
                tv_previous_visit.visibility= View.VISIBLE
                tv_planned_visit.visibility= View.VISIBLE
                tv_override_otp.visibility=View.VISIBLE
                tv_override_otp.paintFlags = tv_manage_otp.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }
        }
        backBtn.setOnClickListener {
            this@VerifyVisitorActivity.finish()
        }

        tv_planned_visit.setOnClickListener {
            val intent = Intent(this@VerifyVisitorActivity, PlannedVisitActivity::class.java)
            intent.putExtra("Visit_Screen","Planned")
            intent.putExtra("mobile",et_user_name.text.toString())
            startActivity(intent)
        }
        tv_previous_visit.setOnClickListener {
            val intent = Intent(this@VerifyVisitorActivity, PlannedVisitActivity::class.java)
            intent.putExtra("Visit_Screen","Previous")
            intent.putExtra("mobile",et_user_name.text.toString())
            startActivity(intent)
        }
        tv_manage_otp.setOnClickListener {
            progressViewState.progressbarEvent=true

            var mobile=et_user_name.text.toString()
            val myDB = FirebaseFirestore.getInstance().collection(Constants.MANAGE_OTP)
            val query1: Query = myDB.whereEqualTo(Constants.VISITOR_MOBILE,mobile)
            query1.get().addOnSuccessListener {
                    result ->
                if(result.isEmpty) {
                    progressViewState.progressbarEvent=false
                    addToManageOTP(mobile)
                }else {
                    for (document in result) {
                        progressViewState.progressbarEvent=false
                        val resultMobileNo = document.data[Constants.VISITOR_MOBILE].toString()
                        if (mobile == resultMobileNo) {
                            val intent =
                                Intent(
                                    this@VerifyVisitorActivity,
                                    VisitorsOTPFirbActivity::class.java
                                )
                            startActivity(intent)
                            this@VerifyVisitorActivity.finish()
                            break
                        } else {
                            addToManageOTP(mobile)


                        }

                    }
                }

            }.addOnFailureListener {
                progressViewState.progressbarEvent=false
                toast(R.string.msg_something_went)
            }


        }

        btn_send_otp.setOnClickListener {
            pg_bar_send_otp.visibility=View.VISIBLE
            btn_send_otp.visibility=View.GONE
            startPhoneNumberVerification( "+91" + et_user_name.text.toString())
        }
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                pg_bar_send_otp.visibility=View.GONE
                btn_send_otp.visibility=View.VISIBLE
                Log.d(TAG, "onVerificationCompleted:$credential")
              //  signInWithPhoneAuthCredential(credential,et_user_name.text.toString())
            }

            override fun onVerificationFailed(e: FirebaseException) {


                pg_bar_send_otp.visibility=View.GONE
                btn_send_otp.visibility=View.VISIBLE
                fm_layout_verify_btn.visibility=View.VISIBLE
                tv_manage_otp.visibility=View.VISIBLE
                tv_manage_otp.paintFlags = tv_manage_otp.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                etOtp.visibility=View.VISIBLE
                et_user_name.isClickable = false
                et_user_name.isEnabled = false

                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                val toast: Toast= Toast.makeText(this@VerifyVisitorActivity, e.message, Toast.LENGTH_SHORT)
                toast.show()


            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                pg_bar_send_otp.visibility=View.GONE
                btn_send_otp.visibility=View.VISIBLE
                Log.d(TAG, "onCodeSent:$verificationId")

                storedVerificationId = verificationId
                resendToken = token

                fm_layout_verify_btn.visibility=View.VISIBLE
                tv_manage_otp.visibility=View.VISIBLE
                tv_manage_otp.paintFlags = tv_manage_otp.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                etOtp.visibility=View.VISIBLE
                et_user_name.isClickable = false
                et_user_name.isEnabled = false
                showToast(R.string.msg_otp_sent)
            }
        }

        btn_verify_otp.setOnClickListener {
            if (etOtp.text.isNotEmpty()) {
                progressViewState.progressbarEvent = true
                val credential =
                    PhoneAuthProvider.getCredential(storedVerificationId!!, etOtp.text.toString())
                signInWithPhoneAuthCredential(credential, et_user_name.text.toString())
            }else{
                showToast(R.string.text_enter_otp)
            }

        }
    }
    fun addToManageOTP(mobile:String){
        val db = Firebase.firestore
        val manageOtp = hashMapOf(
            Constants.VISITOR_MOBILE to mobile,
            Constants.TIMESTAMP to FieldValue.serverTimestamp(),
        )
        db.collection(Constants.MANAGE_OTP)
            .add(manageOtp)
            .addOnSuccessListener {
                val intent =
                    Intent(
                        this@VerifyVisitorActivity,
                        VisitorsOTPFirbActivity::class.java
                    )
                startActivity(intent)
                this@VerifyVisitorActivity.finish()
            }
            .addOnFailureListener { exception ->
                Log.w("users error>>", "Error getting documents.", exception)
            }
    }
    private  fun checkPreviousVisits(tvPrevoius:TextView,mobile:String){
        var timeStamp=  FieldValue.serverTimestamp()
        Log.d("inTimeStamp>>>>",timeStamp.toString())

        val myDB = FirebaseFirestore.getInstance().collection(Constants.VISITOR_LIST)
        val query1: Query = myDB.whereEqualTo(Constants.VISITOR_MOBILE,mobile)
        query1.get().addOnSuccessListener { result ->
            if(result.isEmpty) {
                Log.d("inPKPrevEmpty>>>>","empty")
                tvPrevoius.alpha = 0.5f
                tvPrevoius.isClickable = false
                tvPrevoius.isEnabled = false
            } else {
                Log.d("inPKPrevnonempty>>>>","non-empty")
                for (document in result) {
                    if (checkDateBetween(document.data[Constants.VISIT_DATE].toString(), getCurrentDate())) {
                        Log.d("inPKPrevhost>>>>","PK")
                        tvPrevoius.alpha = 1f
                        tvPrevoius.paintFlags = tvPrevoius.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    }
                }

            }
        }.addOnFailureListener {
           Log.d("error", it.message.toString());
        }
    }


    private fun checkDateBetween(readableDateString: String, fromDate: String): Boolean {
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
        val readableDate = dateFormat.parse(readableDateString)
        val todayDate = dateFormat.parse(fromDate)
        return (readableDate.before(todayDate) )
    }

    private  fun checkPlannedVisits(tvPlanned:TextView,mobile:String){

        val myDB = FirebaseFirestore.getInstance().collection(Constants.PLANNED_VISIT)
        val query1: Query = myDB.whereEqualTo(Constants.VISITOR_MOBILE,mobile)
        query1.get().addOnSuccessListener { result ->
            if(result.isEmpty) {
                progressViewState.progressbarEvent=false
                Log.d("inPKPlanned>>>>","empty")
                tvPlanned.alpha = 0.5f
                tvPlanned.isClickable = false
                tvPlanned.isEnabled = false
            } else {
                progressViewState.progressbarEvent=false
                Log.d("inPKPlanned>>>>","non-empty")
                for (document in result) {
                    if (checkPlannedDate(document.data[Constants.VISIT_DATE].toString(), getCurrentDate())) {
                        Log.d("inPKPlannedhost>>>>","PK")
                        tvPlanned.alpha = 1f
                        tvPlanned.paintFlags = tvPlanned.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    }
                }

            }
        }.addOnFailureListener {
            progressViewState.progressbarEvent=false
            Log.d("error", it.message.toString());
        }
    }
    private fun checkPlannedDate(readableDateString: String, fromDate: String): Boolean {
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
        val readableDate = dateFormat.parse(readableDateString)
        val todayDate = dateFormat.parse(fromDate)
        return (readableDate.after(todayDate)|| readableDate.equals(todayDate))
    }
    private fun checkBlackListedMobile(mobile: String){
      //  var isExisting = false
        isBlackListed=false
        val myDB = FirebaseFirestore.getInstance().collection(Constants.BLACKLIST_VISITOR)
        val query1: Query =
            myDB.whereEqualTo(Constants.MOBILE_N0, mobile)
        query1.get().addOnSuccessListener { result ->
            for(document in result) {
                val resultMobileNo = document.data[Constants.MOBILE_N0].toString()
                if(mobile==resultMobileNo){
                    isBlackListed=true
                    Log.d("isExistingT>>>>",isBlackListed.toString())
                    break
                }else{
                    isBlackListed=false
                    Log.d("isExistingF>>>>",isBlackListed.toString())

                }
              //  isBlackListed = resultMobileNo == mobile

               // firebaseCallBack.onResponse(isExisting)
            }

        }.addOnFailureListener {
            toast(R.string.msg_something_went)
        }



    }

    interface FirebaseCallback {
        fun onResponse(value: Boolean?)
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
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential,mobile:String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val db=Firebase.firestore

                    var query=db.collection(Constants.MANAGE_OTP).whereEqualTo(Constants.VISITOR_MOBILE,mobile)
                        .get()
                    progressViewState.progressbarEvent=false
                    if (query.isSuccessful){
                        query.addOnSuccessListener {
                            for (document in it){
                                db.collection(Constants.MANAGE_OTP).document(document.id).delete()
                                val intent = Intent(this@VerifyVisitorActivity, RegisterVisitorActivity::class.java)
                                intent.putExtra("mobile",mobile)
                                startActivity(intent)
                                this@VerifyVisitorActivity.finish()

                            }
                        } .addOnFailureListener { exception ->
                            Log.w("checkout error>>", "Error getting documents.", exception)

                        }
                    }else{
                        val intent = Intent(this@VerifyVisitorActivity, RegisterVisitorActivity::class.java)
                        intent.putExtra("mobile",mobile)
                        startActivity(intent)
                        this@VerifyVisitorActivity.finish()
                    }


                } else {
                    progressViewState.progressbarEvent=false
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    toast(R.string.msg_otp_invalid)

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun markButtonDisable(button: Button) {
       // button.alpha = 0.5f
        button.setBackgroundResource(R.drawable.grey_button)

        button.isEnabled = false

        button.isClickable = false
        }

    private fun showToast(msg:Int){
        val toast: Toast= Toast.makeText(this@VerifyVisitorActivity, msg, Toast.LENGTH_SHORT)
        toast.show()
    }


}


