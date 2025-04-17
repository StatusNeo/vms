package com.android.visitormanagementsystem.ui.plannedvisit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.visitormanagementsystem.PlannedVisitBinding
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.ui.adapters.PlannedVisitAdapter
import com.android.visitormanagementsystem.ui.adapters.loadImage
import com.android.visitormanagementsystem.ui.interfaces.VisitorDataInterface

class PlannedVisitActivity : AppCompatActivity() , VisitorDataInterface {

    lateinit var plannedVisitViewModel : PlannedVisitViewModel
    lateinit var binding:PlannedVisitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        plannedVisitViewModel = ViewModelProvider(this)[PlannedVisitViewModel::class.java]
        val screenIntent = intent.getStringExtra("Visit_Screen")
        setContentView(PlannedVisitBinding.inflate(layoutInflater).apply {
            progressbarViewState = plannedVisitViewModel.plannedVisitViewState
            binding=this
            var mobile_no = intent.getStringExtra("mobile")
            viewState =  plannedVisitViewModel.viewState

            if(screenIntent.contentEquals("Planned")) {
              //  plannedVisitViewModel.setVisitorProfile(mobile_no ?: "")
                tvToolbarTitle.text = "Planned Visits"
                plannedVisitViewModel.checkPlannedVisits(mobile_no ?: "")
            } else {
                tvToolbarTitle.text = "Previous Visits"
                ivVisitor.visibility=View.VISIBLE
                plannedVisitViewModel.checkPreviousVisits(mobile_no ?: "")
            }
            with(plannedVisitRecyclerView) {
                adapter = PlannedVisitAdapter()
            }

            backBtn.setOnClickListener {
                this@PlannedVisitActivity.finish()
            }
            plannedVisitViewModel.setReportInterface(this@PlannedVisitActivity)
        }.root)
    }

    override fun onData(uiModel: PlannedVisitVisitorDataModel) {
        plannedVisitViewModel.setPreviousVisitorProfile(uiModel)
        binding.ivVisitor.loadImage(uiModel.visitorImage.toString())
        binding.NameTextView.text=uiModel.visitorName
        binding.PhoneNoTextView.text=getString(R.string.mobile_number, uiModel.visitorMobileNo)
    }
}