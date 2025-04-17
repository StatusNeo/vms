package com.android.visitormanagementsystem.ui.host.visitorreports

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.visitormanagementsystem.R
 import com.android.visitormanagementsystem.databinding.ActivityHostVisitorReportsBinding
import com.android.visitormanagementsystem.ui.adapters.VisitorReportsAdapter

import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.Prefs
import com.android.visitormanagementsystem.utils.Prefs.userMobileNo
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VisitorReportsActivity:AppCompatActivity() {
    private lateinit var visitorReportViewModel: VisitorReportsViewModel
    lateinit var ada : VisitorReportsAdapter
    var visitorReportsViewState = ProgressBarViewState()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitorReportViewModel = ViewModelProvider(this)[VisitorReportsViewModel::class.java]
        setContentView(ActivityHostVisitorReportsBinding.inflate(layoutInflater).apply {
            viewState = visitorReportsViewState
          //  visitorReportViewModel.setReportInterface(this@HostReportsActivity)
            with(hostRecyclerView) {
                var items: ArrayList<VisitorReportsUIModel> = ArrayList()
                layoutManager = LinearLayoutManager(context)
                adapter = VisitorReportsAdapter(items)
                ada = adapter as VisitorReportsAdapter
            }
            btnProceed.setOnClickListener {
                if(btnSelectDate.text.toString() == "Select Date") {
                    toast(R.string.msg_select_date)
                } else {
                    visitorReportsViewState.progressbarEvent = true
                    val prefs =
                        Prefs.customPreference(this@VisitorReportsActivity, Constants.LoggedIn_Pref)
                    visitorReportViewModel.initHostReport(
                        btnSelectDate.text.toString(),
                        prefs.userMobileNo.toString(),
                    )
                }
            }
            setDatePickerDialog(this)
        }.root)

    }

    private fun setDatePickerDialog(reportsBinding: ActivityHostVisitorReportsBinding) {
        // create an OnDateSetListener
        var calendar = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { dateView, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
                reportsBinding.btnSelectDate.text = sdf.format(calendar.getTime())
            }

        reportsBinding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                this@VisitorReportsActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }


}