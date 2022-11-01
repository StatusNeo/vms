package com.android.visitormanagementsystem.ui.host.hostreports

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityHostReportsBinding
import com.android.visitormanagementsystem.ui.adapters.HostReportAdapter
import com.android.visitormanagementsystem.ui.adminreports.AdminReportsUiModel
import com.android.visitormanagementsystem.ui.interfaces.OnHostReportClickInterface
import com.android.visitormanagementsystem.ui.interfaces.OnReportDownloadInterface
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.showLogoutDialog
import com.android.visitormanagementsystem.utils.toast
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class HostReportsActivity : AppCompatActivity(), OnReportDownloadInterface {
    private lateinit var hostReportViewModel: HostReportsViewModel
    lateinit var ada : HostReportAdapter
    var hostReportsViewState = ProgressBarViewState()
    lateinit var binding : ActivityHostReportsBinding
    var selectedDate: String = ""
    var hostMobileNo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hostReportViewModel = ViewModelProvider(this)[HostReportsViewModel::class.java]
        setContentView(ActivityHostReportsBinding.inflate(layoutInflater).apply {
            binding = this
            viewState = hostReportsViewState
            hostMobileNo =  intent.getStringExtra("mobile").toString()
            hostReportViewModel.setReportInterface(this@HostReportsActivity)
            with(hostRecyclerView) {
                var items: ArrayList<HostReportUiModel> = ArrayList()
                layoutManager = LinearLayoutManager(context)
                adapter = HostReportAdapter(items, object : OnHostReportClickInterface{
                    override fun onVisitorClick(uiModel: HostReportUiModel, pos: Int) {
                        showDialog(uiModel,context)
                    }
                })
                ada = adapter as HostReportAdapter
            }
            setDatePickerDialog(this)
            searchByName()
            ivLogout.setOnClickListener{
               /* val intent = Intent(this@HostReportsActivity, VisitorLandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                this@HostReportsActivity.finish()*/
                showLogoutDialog(this@HostReportsActivity)
            }

        }.root)
    }

    private fun showDialog(model: HostReportUiModel, context : Context) {
        val dialog = Dialog(context, R.style.Theme_Dialog)
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
        checkoutGenderTextView.text = model.visittime

        val checkoutBatchNoTextView = dialog.findViewById(R.id.checkoutBatchNoTextView) as TextView
        checkoutBatchNoTextView.text = model.batchNo

        dialog.setCancelable(true)
        dialog.show()
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
                selectedDate = sdf.format(calendar.getTime())
                if(selectedDate.isEmpty()) {
                    toast(R.string.msg_select_date)
                }else {
                    binding.btnProceed.isEnabled = false
                    hostReportsViewState.progressbarEvent = true
                    hostReportViewModel.initHostReport(
                        selectedDate,
                        hostMobileNo,
                    )
                    binding.hostRecyclerView.visibility = View.VISIBLE
                }
            }

        reportsBinding.btnProceed.setOnClickListener {
            binding.etSearchName.setText("")
            DatePickerDialog(
                this@HostReportsActivity,R.style.ThemeOverlay_App_MaterialCalendar,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun searchByName(){
        binding.etSearchName.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if((event?.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    var enteredName = binding.etSearchName.text.toString()
                    if(enteredName.isNotBlank()) {
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.hideSoftInputFromWindow(p0?.windowToken, 0)

                        hostReportsViewState.progressbarEvent = true
                        hostReportViewModel.searchByName(enteredName, hostMobileNo)
                    }
                    return true
                }
                return false
            }
        })
    }


    override fun openReportScreen(items: List<HostReportUiModel>) {
        hostReportsViewState.progressbarEvent = false
        binding.btnProceed.isEnabled = true
        if(items.isEmpty()){
            toast(R.string.msg_no_data)
            binding.noDataTv.visibility = View.VISIBLE
            binding.noDataView.visibility = View.VISIBLE
        }else{
            binding.noDataTv.visibility = View.GONE
            binding.noDataView.visibility = View.GONE
            binding.hostRecyclerView.visibility = View.VISIBLE
        }
            ada.setUserList(items)
    }
}