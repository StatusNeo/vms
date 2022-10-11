package com.android.visitormanagementsystem.ui.adminreports

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityAdminReportsBinding
import com.android.visitormanagementsystem.ui.adapters.AdminReportsAdapter
import com.android.visitormanagementsystem.ui.interfaces.OnAdminReportInterface
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import java.text.SimpleDateFormat
import java.util.*

class AdminReportsActivity : AppCompatActivity(), OnAdminReportInterface {
    private lateinit var adminViewModel: AdminReportsViewModel
    var calendar = Calendar.getInstance()
    var fromHour: Int? = 0
    var fromMins: Int? = 0
    lateinit var listAdapter: AdminReportsAdapter
    var adminReportsViewState = ProgressBarViewState()

    lateinit var binding : ActivityAdminReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adminViewModel = ViewModelProvider(this)[AdminReportsViewModel::class.java]
        setContentView(ActivityAdminReportsBinding.inflate(layoutInflater).apply {
            binding = this
            viewStateProgress = adminReportsViewState
            adminViewModel.setReportInterface(this@AdminReportsActivity)

            setDatePickerDialog(this)
            setFromTimePickerDialog(this)
            setToTimePickerDialog(this)

            with(homeReportsRecyclerView) {
                var items: ArrayList<AdminReportsUiModel> = ArrayList()
                layoutManager = LinearLayoutManager(context)
                adapter = AdminReportsAdapter(items)
                listAdapter = adapter as AdminReportsAdapter
            }

         /*   backBtn.setOnClickListener {
                this@AdminReportsActivity.finish()
            }
*/
            btnProceed.setOnClickListener {
                if(tvSelectDate.text == "Select Date") {
                    toast(R.string.msg_select_date)
                } /*else if(tvFromTime.text == "From Time") {
                    toast(R.string.msg_select_from_time)
                } else if(tvToTime.text == "To Time") {
                    toast(R.string.msg_select_to_time)
                }*/ else {
                    adminReportsViewState.progressbarEvent = true
                    binding.btnProceed.isEnabled = false
                    adminViewModel.initVisitorList(tvSelectDate.text.toString())
                    homeReportsRecyclerView.visibility = View.VISIBLE
                }
            }

        }.root)

    }

    private fun setFromTimePickerDialog(activityPlanVisitBinding: ActivityAdminReportsBinding) {

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            // timeView, hourOfDay, minute -> activityPlanVisitBinding.tvFromTime.text = (String.format("%d : %d", hourOfDay, minute))
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

                fromHour = hourOfDay
                fromMins = minute
                setTime(hourOfDay, minute, true, activityPlanVisitBinding)

            }
        }, hour, minute, false)

        /* activityPlanVisitBinding.ivInTime.setOnClickListener {
             mTimePicker.show()
         }
         activityPlanVisitBinding.tvFromTime.setOnClickListener {
             mTimePicker.show()
         }*/

    }

    private fun setToTimePickerDialog(activityPlanVisitBinding: ActivityAdminReportsBinding) {

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            // timeView, hourOfDay, minute -> activityPlanVisitBinding.tvToTime.text = (String.format("%d : %d", hourOfDay, minute))
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                if((hourOfDay <= fromHour!!) && (minute <= fromMins!!)) {
                    toast(R.string.text_to_time_error)
                } else {
                    setTime(hourOfDay, minute, false, activityPlanVisitBinding)
//                       activityPlanVisitBinding.tvToTime.text =
//                           String.format("%d : %d", hourOfDay, minute)
                }
            }
        }, hour, minute, false)

        /*    activityPlanVisitBinding.ivOutTime.setOnClickListener {
                mTimePicker.show()
            }
            activityPlanVisitBinding.tvToTime.setOnClickListener {
                mTimePicker.show()
            }*/
    }

    private fun setTime(
        hourOfDay: Int,
        minute: Int,
        fromTime: Boolean,
        activityPlanVisitBinding: ActivityAdminReportsBinding
    ) {
        var hour = hourOfDay
        var min = minute
        var am_pm = ""

        // AM_PM decider logic
        when {
            hour == 0 -> {
                hour += 12
                am_pm = "AM"
            }
            hour == 12 -> am_pm = "PM"
            hour > 12 -> {
                hour -= 12
                am_pm = "PM"
            }
            else -> am_pm = "AM"
        }
        val hourt = if(hour < 10) "0" + hour else hour
        val mint = if(minute < 10) "0" + minute else minute
        // display format of time
        val time = "$hourt : $mint $am_pm"

        /*  if(fromTime) {
              activityPlanVisitBinding.tvFromTime.text = time

          } else {
              activityPlanVisitBinding.tvToTime.text = time
          }*/

    }

    private fun setDatePickerDialog(activityReportsBinding: ActivityAdminReportsBinding) {
        // create an OnDateSetListener
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { dateView, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
                activityReportsBinding.tvSelectDate.text = sdf.format(calendar.getTime())
            }

        activityReportsBinding.ivCalender.setOnClickListener {
            DatePickerDialog(
                this@AdminReportsActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        activityReportsBinding.tvSelectDate.setOnClickListener {
            DatePickerDialog(
                this@AdminReportsActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

    }

    override fun openReportScreen(items: List<AdminReportsUiModel>) {
        adminReportsViewState.progressbarEvent = false
        binding.btnProceed.isEnabled = true
        if(items.isEmpty()) {
            toast(R.string.msg_no_data)
        }
        listAdapter.setUserList(items)
    }
}