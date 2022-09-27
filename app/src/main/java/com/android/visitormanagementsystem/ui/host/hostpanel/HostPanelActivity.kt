package com.android.visitormanagementsystem.ui.host.hostpanel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.visitormanagementsystem.databinding.ActivityHostPanelBinding
import com.android.visitormanagementsystem.ui.host.hostreports.HostReportsActivity
import com.android.visitormanagementsystem.ui.host.planvisit.PlanVisitActivity
import com.android.visitormanagementsystem.ui.host.visitorreports.VisitorReportsActivity
import com.android.visitormanagementsystem.utils.showLogoutDialog

class HostPanelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityHostPanelBinding.inflate(layoutInflater).apply {

            btnPlanVisit.setOnClickListener {
                startActivity(Intent(this@HostPanelActivity, PlanVisitActivity::class.java))
            }
            btnReports.setOnClickListener{
               startActivity(Intent(this@HostPanelActivity, HostReportsActivity::class.java))
            }
            btnVisitors.visibility=View.GONE
            btnVisitors.setOnClickListener{
                startActivity(Intent(this@HostPanelActivity, VisitorReportsActivity::class.java))
            }
            backBtn.setOnClickListener {
                this@HostPanelActivity.finish()
            }
            hostLogoutButton.setOnClickListener{
                showLogoutDialog(this@HostPanelActivity)
            }
        }.root)
    }
}