package com.android.visitormanagementsystem.ui.blacklistedvisitor

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityBlacklistedVisitorBinding
import com.android.visitormanagementsystem.ui.adapters.BlacklistedVisitorAdapter
import com.android.visitormanagementsystem.ui.interfaces.OnBlacklistVisitorClickInterface
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import java.text.SimpleDateFormat
import java.util.*

class BlacklistedVisitorActivity:AppCompatActivity(), OnBlacklistVisitorClickInterface{
    private lateinit var blockedViewModel: BlackListedVisitorViewModel
    lateinit var listAdapter : BlacklistedVisitorAdapter
    var blacklistedVisitorViewState = ProgressBarViewState()
    var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockedViewModel = ViewModelProvider(this)[BlackListedVisitorViewModel::class.java]
        setContentView(ActivityBlacklistedVisitorBinding.inflate(layoutInflater).apply {

            viewState = blacklistedVisitorViewState
            blockedViewModel.setVisitorInterface(this@BlacklistedVisitorActivity)
            setDatePickerDialog(this)
            setToDatePickerDialog(this)

            with(homeReportsRecyclerView ) {

                val items: ArrayList<BlackListedVisitorUiModel> = ArrayList()
                layoutManager = LinearLayoutManager(context)
                adapter = BlacklistedVisitorAdapter(items)
                listAdapter = adapter as BlacklistedVisitorAdapter
            }

            backBtn.setOnClickListener {
                this@BlacklistedVisitorActivity.finish()
            }

            btnProceed.setOnClickListener {
                if (tvFromDate.text != "From Date" || tvToDate.text != "To Date") {
                    if (tvFromDate.text=="From Date"){
                        toast(R.string.msg_from_date)
                    }else if (tvToDate.text=="To Date"){
                        toast(R.string.msg_to_date)
                    } else if(checkToDateLessThanFromDate(tvToDate.text.toString(), tvFromDate.text.toString())){
                        toast(R.string.to_date_should_be_less_than_from_date)
                    } else{
                        blacklistedVisitorViewState.progressbarEvent = true
                        homeReportsRecyclerView.visibility= View.VISIBLE
                        blockedViewModel.initVisitorList(tvFromDate.text.toString(), tvToDate.text.toString())
                    }
                } else {
                    toast(R.string.msg_select_dates)
                }
            }

        }.root)
    }

    private fun setDatePickerDialog(activityReportsBinding: ActivityBlacklistedVisitorBinding) {
        // create an OnDateSetListener
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { dateView, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
                activityReportsBinding.tvFromDate.text = sdf.format(calendar.getTime())
            }

        activityReportsBinding.tvFromDate.setOnClickListener {
            DatePickerDialog(this@BlacklistedVisitorActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()        }

    }
    private fun setToDatePickerDialog(activityReportsBinding: ActivityBlacklistedVisitorBinding) {
        // create an OnDateSetListener
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { dateView, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
                activityReportsBinding.tvToDate.text = sdf.format(calendar.getTime())
            }

        activityReportsBinding.tvToDate.setOnClickListener {

            if (activityReportsBinding.tvFromDate.text == "From Date"){
                toast(R.string.msg_from_date)
            }else {
                DatePickerDialog(
                    this@BlacklistedVisitorActivity,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    private fun checkToDateLessThanFromDate(toDateString: String, fromDate: String): Boolean {
        val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
        val readableDate = dateFormat.parse(toDateString)
        val todayDate = dateFormat.parse(fromDate)
        return (readableDate.before(todayDate) )
    }

    override fun openReportScreen(items: List<BlackListedVisitorUiModel>) {
        blacklistedVisitorViewState.progressbarEvent = false
        if(items.isEmpty()){
            toast(R.string.msg_no_data)
        }
        listAdapter.setUserList(items)
    }
}