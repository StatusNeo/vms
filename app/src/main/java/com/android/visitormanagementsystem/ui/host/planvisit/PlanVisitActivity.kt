package com.android.visitormanagementsystem.ui.host.planvisit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.ActivityPlanVisitBinding
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.utils.*
import com.android.visitormanagementsystem.utils.Prefs.hostName
import com.android.visitormanagementsystem.utils.Prefs.userMobileNo
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class PlanVisitActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    var calendar = Calendar.getInstance()
    var calendarForMindate = Calendar.getInstance()
    var selectedDate : String = ""
    var selectedTime : String = ""
    var progressBarViewState = ProgressBarViewState()
    private lateinit var planVisitViewModel: PlanVisitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        planVisitViewModel = ViewModelProvider(this)[PlanVisitViewModel::class.java]
        val prefs = Prefs.customPreference(this@PlanVisitActivity, Constants.LoggedIn_Pref)
        planVisitViewModel.getAllPlanVisit(prefs.userMobileNo.toString())

        setContentView(ActivityPlanVisitBinding.inflate(layoutInflater).apply {
            viewState = progressBarViewState
            setDatePickerDialog(this)
            setTimePickerDialog(this)
            btnNext.setOnClickListener {
                this@PlanVisitActivity.finish()
            }
            btnNext.setOnClickListener{
                if(etMobileNumber.text.isNullOrBlank() && etVisitorName.text.isNullOrBlank()
                    && etVisitorCompanyname.text.isNullOrBlank()) {
                    etMobileNumber.requestFocus()
                    etMobileNumber.error = getString(R.string.mobile_empty_prompt)
                    etVisitorName.error = getString(R.string.please_enter_user_name)
                    etVisitorCompanyname.error = getString(R.string.please_enter_company)
                }
                else if(etMobileNumber.text.isNullOrBlank()) {
                    etMobileNumber.requestFocus()
                    etMobileNumber.error = getString(R.string.mobile_empty_prompt)
                }else if (etMobileNumber.text?.length!=10){
                    etMobileNumber.error="Mobile number should be 10 digit's"
                }else if(!isValidPhoneNumber(etMobileNumber.text.toString())){
                    etMobileNumber.error = "Invalid Mobile Number"
                }
                else if(etVisitorName.text.isNullOrBlank()) {
                    etVisitorName.requestFocus()
                    etVisitorName.error = getString(R.string.please_enter_user_name)
                }  else if(etVisitorCompanyname.text.isNullOrBlank()) {
                    etVisitorCompanyname.requestFocus()
                    etVisitorCompanyname.error = getString(R.string.please_enter_company)
                } else if(selectedDate.isNullOrBlank()) {
                    showToast("Please select visit date.")
                } else if(selectedTime.isNullOrBlank()) {
                    showToast("Please select visit time.")
                }else if(etVisitorPurpose.text.isNullOrBlank()){
                    etVisitorPurpose.requestFocus()
                    etVisitorPurpose.error="Please enter purpose of visit"
                }
                else {
                    progressBarViewState.progressbarEvent = true
                    val visitor = hashMapOf(
                        Constants.HOST_MOBILE to prefs.userMobileNo.toString(),
                        "visitorMobileNo" to etMobileNumber.text.toString(),
                        "visitorName" to etVisitorName.text.toString() ,
                        "visitorCompanyName" to etVisitorCompanyname.text.toString(),
                        "visitDate" to selectedDate,
                        "inTime" to selectedTime,
                        "purpose" to etVisitorPurpose.text.toString(),
                        Constants.TIMESTAMP to FieldValue.serverTimestamp(),
                        Constants.HOST_NAME to prefs.hostName.toString(),
                        )

                    db.collection(Constants.PLANNED_VISIT)
                        .add(visitor)
                        .addOnSuccessListener {
                            progressBarViewState.progressbarEvent = false
                            toast(R.string.toast_plan_visit)
                            this@PlanVisitActivity.finish()
                        }
                        .addOnFailureListener { exception ->
                            progressBarViewState.progressbarEvent = false
                            Log.w("users error>>", "Error getting documents.", exception)
                        }
                }
            }
        }.root)

    }

    fun isValidPhoneNumber(phoneNo : String) : Boolean {
        return Pattern.compile(Constants.MOBILE_NUMBER_REGEX).matcher(phoneNo).matches()
    }

    private fun showToast(msg:String){
        val toast: Toast = Toast.makeText(this@PlanVisitActivity, msg, Toast.LENGTH_SHORT)
        toast.show()
    }
    private fun setTime(hourOfDay: Int, minute: Int,activityPlanVisitBinding: ActivityPlanVisitBinding){

        var hour=hourOfDay
        var min=minute
        var am_pm = ""

        // AM_PM decider logic
        when {hour == 0 -> {
            hour+=12
            am_pm = "AM"
        }
            hour == 12 -> am_pm = "PM"
            hour > 12 -> {
                hour -= 12
                am_pm = "PM"
            }
            else -> am_pm = "AM"
        }
        val hourt = if (hour < 10) "0" + hour else hour
        val mint = if (minute < 10) "0" + minute else minute
        // display format of time
        val time = "$hourt : $mint $am_pm"
            activityPlanVisitBinding.btnSelectTime.text=time
    }

    private fun setTimePickerDialog(activityPlanVisitBinding: ActivityPlanVisitBinding) {

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minuteOfDay: Int) {

                if(selectedDate == getCurrentDate()) {
                    if(hourOfDay <= hour && minuteOfDay <= minute) {
                        toast(R.string.text_time)

                    } else {
                        setTime(hourOfDay, minuteOfDay, activityPlanVisitBinding)
                        selectedTime = activityPlanVisitBinding.btnSelectTime.text.toString()
                    }
                }else{
                    setTime(hourOfDay, minuteOfDay, activityPlanVisitBinding)
                    selectedTime = activityPlanVisitBinding.btnSelectTime.text.toString()
                }
            }
        }, hour, minute, false)
        
        activityPlanVisitBinding.btnSelectTime.setOnClickListener {
            if(!selectedDate.isNullOrBlank()) {
                mTimePicker.show()
            }else{
                showToast("Please select visit date first.")
            }
        }
    }

    private fun setDatePickerDialog(activityPlanVisitBinding: ActivityPlanVisitBinding) {
        // create an OnDateSetListener
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { dateView, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
                activityPlanVisitBinding.btnSelectDate.text = sdf.format(calendar.getTime())
                selectedDate = activityPlanVisitBinding.btnSelectDate.text.toString()
            }

        activityPlanVisitBinding.btnSelectDate.setOnClickListener {
          var mDialog =  DatePickerDialog(this@PlanVisitActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            mDialog.datePicker.minDate = calendarForMindate.timeInMillis
            mDialog .show()
        }
    }
}