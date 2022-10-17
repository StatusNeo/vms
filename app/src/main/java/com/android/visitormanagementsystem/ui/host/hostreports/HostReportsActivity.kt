package com.android.visitormanagementsystem.ui.host.hostreports

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityHostReportsBinding
import com.android.visitormanagementsystem.ui.adapters.HostReportAdapter
import com.android.visitormanagementsystem.ui.interfaces.OnReportDownloadInterface
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.Prefs
import com.android.visitormanagementsystem.utils.Prefs.userMobileNo
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HostReportsActivity : AppCompatActivity(), OnReportDownloadInterface {
    private lateinit var hostReportViewModel: HostReportsViewModel
    lateinit var ada : HostReportAdapter
    var hostReportsViewState = ProgressBarViewState()
    lateinit var binding : ActivityHostReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hostReportViewModel = ViewModelProvider(this)[HostReportsViewModel::class.java]
        setContentView(ActivityHostReportsBinding.inflate(layoutInflater).apply {
            binding = this
            viewState = hostReportsViewState
            hostReportViewModel.setReportInterface(this@HostReportsActivity)
            with(hostRecyclerView) {
                var items: ArrayList<HostReportUiModel> = ArrayList()
                layoutManager = LinearLayoutManager(context)
                adapter = HostReportAdapter(items)
                ada = adapter as HostReportAdapter
            }

            btnProceed.setOnClickListener {
                if(btnSelectDate.text.toString() == "Select Date") {
                    toast(R.string.msg_select_date)
                } else {
                    binding.btnProceed.isEnabled = false
                    hostReportsViewState.progressbarEvent = true
                        hostReportViewModel.initHostReport(
                        btnSelectDate.text.toString(),
                        intent.getStringExtra("mobile").toString(),
                    )
                }
            }
            setDatePickerDialog(this)
            homeBtn.setOnClickListener{
                val intent = Intent(this@HostReportsActivity, VisitorLandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                this@HostReportsActivity.finish()
            }

        }.root)

    }

    override fun onBackPressed() {
        val intent = Intent(this@HostReportsActivity, VisitorLandingActivity::class.java)
        startActivity(intent)
        this@HostReportsActivity.finish()
    }

    private fun setDatePickerDialog(reportsBinding: ActivityHostReportsBinding) {
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
                this@HostReportsActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    override fun openReportScreen(items: List<HostReportUiModel>) {
        hostReportsViewState.progressbarEvent = false
        binding.btnProceed.isEnabled = true
        if(items.isEmpty()){
            toast(R.string.msg_no_data)
        }
            ada.setUserList(items)
    }
}