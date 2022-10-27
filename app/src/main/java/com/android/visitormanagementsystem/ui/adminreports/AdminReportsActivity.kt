package com.android.visitormanagementsystem.ui.adminreports

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityAdminReportsBinding
import com.android.visitormanagementsystem.ui.adapters.AdminReportsAdapter
import com.android.visitormanagementsystem.ui.addhostprofile.AddHostActivity
import com.android.visitormanagementsystem.ui.adminpanel.AdminPanelActivity
import com.android.visitormanagementsystem.ui.gethost.HostProfileUiModel
import com.android.visitormanagementsystem.ui.interfaces.OnAdminReportInterface
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorReportClickInterface
import com.android.visitormanagementsystem.ui.visitorList.VisitorListActivity
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AdminReportsActivity : AppCompatActivity(), OnAdminReportInterface {
    private lateinit var adminViewModel: AdminReportsViewModel
    var calendar = Calendar.getInstance()
    var fromHour: Int? = 0
    var fromMins: Int? = 0
    lateinit var listAdapter: AdminReportsAdapter
    var adminReportsViewState = ProgressBarViewState()
    var selectedDate: String = ""
    var calledFrom : String = ""
    lateinit var binding : ActivityAdminReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adminViewModel = ViewModelProvider(this)[AdminReportsViewModel::class.java]
        calledFrom = intent.getStringExtra("Called_From").toString()
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
                adapter = AdminReportsAdapter(items, object : OnVisitorReportClickInterface{
                    override fun onVisitorClick(uiModel: AdminReportsUiModel, pos: Int) {
                        showDialog(uiModel,context)
                    }
                })
                listAdapter = adapter as AdminReportsAdapter
            }

            searchByName()
            btnHome.setOnClickListener {
                if(calledFrom.equals("Admin")){
                    val intent = Intent(this@AdminReportsActivity, AdminPanelActivity::class.java)
                    startActivity(intent)
                    this@AdminReportsActivity.finish()
                }else {
                    val intent = Intent(this@AdminReportsActivity, VisitorListActivity::class.java)
                    startActivity(intent)
                    this@AdminReportsActivity.finish()
                }
            }
            btnAddVisitor.setOnClickListener{
                if(calledFrom.equals("Admin")){
                    val intent = Intent(this@AdminReportsActivity, AddHostActivity::class.java)
                    startActivity(intent)
                }else {
                    val intent =
                        Intent(this@AdminReportsActivity, VisitorLandingActivity::class.java)
                    startActivity(intent)
                }
            }


         /*   backBtn.setOnClickListener {
                this@AdminReportsActivity.finish()
            }
*/
           /* ivCalender.setOnClickListener {
                if(selectedDate.isEmpty()) {
                    toast(R.string.msg_select_date)
                } *//*else if(tvFromTime.text == "From Time") {
                    toast(R.string.msg_select_from_time)
                } else if(tvToTime.text == "To Time") {
                    toast(R.string.msg_select_to_time)
                }*//* else {
                    adminReportsViewState.progressbarEvent = true
                    binding.ivCalender.isEnabled = false
                    adminViewModel.initVisitorList(selectedDate)
                    homeReportsRecyclerView.visibility = View.VISIBLE
                }
            }*/

        }.root)
    }

    private fun searchByName(){
            binding.etSearchName.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if((event?.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        var enteredName = binding.etSearchName.text.toString()
                        if(enteredName.isNotBlank()) {
                            adminViewModel.searchByName(enteredName)
                        }
                        return true
                    }
                    return false
                }
            })
    }

    private fun showDialog(model: AdminReportsUiModel, context : Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.report_dialog)

        val imageView = dialog.findViewById(R.id.checkout_image) as ShapeableImageView

        Picasso.get().load(model.visitorImage).placeholder(R.drawable.profile_icon).into(imageView)

        val checkoutNameTextView = dialog.findViewById(R.id.checkoutNameTextView) as TextView
        checkoutNameTextView.text = model.visitorName

        val checkoutMobileTextView = dialog.findViewById(R.id.checkoutMobileTextView) as TextView
        checkoutMobileTextView.text = model.visitorMobileNo

        val checkoutEmailTextView = dialog.findViewById(R.id.checkoutEmailTextView) as TextView
        checkoutEmailTextView.text = model.hostName

        val checkoutDateTextView = dialog.findViewById(R.id.checkoutDateTextView) as TextView
        checkoutDateTextView.text = model.visitDate

        val checkoutGenderTextView = dialog.findViewById(R.id.checkoutGenderTextView) as TextView
        checkoutGenderTextView.text = model.inTime

        val checkoutBatchNoTextView = dialog.findViewById(R.id.checkoutBatchNoTextView) as TextView
        checkoutBatchNoTextView.text = model.batchNo

        dialog.setCancelable(true)
        dialog.show()
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
             //   activityReportsBinding.tvSelectDate.text = sdf.format(calendar.getTime())
                selectedDate = sdf.format(calendar.getTime())

                if(selectedDate.isEmpty()) {
                    toast(R.string.msg_select_date)
                }else {
                    adminReportsViewState.progressbarEvent = true
                    binding.ivCalender.isEnabled = false
                    adminViewModel.initVisitorList(selectedDate)
                    binding.homeReportsRecyclerView.visibility = View.VISIBLE
                }
            }

        activityReportsBinding.ivCalender.setOnClickListener {
       binding.etSearchName.setText("")
            DatePickerDialog(
                this@AdminReportsActivity,R.style.ThemeOverlay_App_MaterialCalendar,
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
        binding.ivCalender.isEnabled = true
        if(items.isEmpty()) {
            toast(R.string.msg_no_data)
            binding.noDataTv.visibility = View.VISIBLE
            binding.noDataView.visibility = View.VISIBLE
        }else{
            binding.noDataTv.visibility = View.GONE
            binding.noDataView.visibility = View.GONE
            binding.homeReportsRecyclerView.visibility = View.VISIBLE
        }
        println("List size in activity" + items.size)
        listAdapter.setUserList(items)
    }
}