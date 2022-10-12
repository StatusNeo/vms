package com.android.visitormanagementsystem.ui.visitorList

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityVisitorListBinding
import com.android.visitormanagementsystem.ui.adapters.VisitorListAdapter
import com.android.visitormanagementsystem.ui.adminreports.AdminReportsActivity
import com.android.visitormanagementsystem.ui.checkout.CheckoutActivity
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorClickInterface
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorListInterface
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast

class VisitorListActivity : AppCompatActivity(), OnVisitorListInterface {

    private lateinit var homeViewModel: VisitorListViewModel
    var visitorsListViewState = ProgressBarViewState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = ViewModelProvider(this)[VisitorListViewModel::class.java]
        setContentView(ActivityVisitorListBinding.inflate(layoutInflater).apply {
            viewStateProgress = visitorsListViewState
            homeViewModel.setReportInterface(this@VisitorListActivity)

            viewModel = homeViewModel
            viewState = homeViewModel.visitorListViewState
            with(homeReportsRecyclerView) {
                adapter = VisitorListAdapter(homeViewModel, object : OnVisitorClickInterface {
                    override fun onVisitorClick(uiModel: VisitorListUiModel, pos: Int) {
                        // Open Visitor Detail screen
                        var intent = Intent(this@VisitorListActivity, CheckoutActivity::class.java)
                        val gson = Gson()
                        val visitorGson = gson.toJson(uiModel)
                        intent.putExtra("Clicked_Visitor", visitorGson)
                        startActivity(intent)
                        finish()
                    }
                })
            }
            btnViewReport.setOnClickListener {
                val intent = Intent(this@VisitorListActivity, AdminReportsActivity::class.java)
                startActivity(intent)
            }
        }.root)
    }

    override fun onBackPressed() {
        val intent = Intent(this@VisitorListActivity, VisitorLandingActivity::class.java)
        startActivity(intent)
        this@VisitorListActivity.finish()    }

    override fun openVisitorsListScreen(items: List<VisitorListUiModel>) {
        if(items.isEmpty()) {
            toast(R.string.msg_no_data)
        }
    }
}