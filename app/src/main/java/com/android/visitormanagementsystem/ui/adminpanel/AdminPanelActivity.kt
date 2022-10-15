package com.android.visitormanagementsystem.ui.adminpanel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.visitormanagementsystem.databinding.ActivityAdminPanelBinding
import com.android.visitormanagementsystem.ui.addhostprofile.AddHostActivity
import com.android.visitormanagementsystem.ui.adminreports.AdminReportsActivity
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity
import com.android.visitormanagementsystem.utils.showLogoutDialog

class AdminPanelActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityAdminPanelBinding.inflate(layoutInflater).apply {

          /*  btnBlacklisted.setOnClickListener{
                val intent = Intent(this@AdminPanelActivity, BlacklistedVisitorActivity::class.java)
                startActivity(intent)
            }
            btnBlocklist.setOnClickListener {
                val intent = Intent(this@AdminPanelActivity, BlackListVisitorActivity::class.java)
                startActivity(intent)
            }*/
            btnReports.setOnClickListener{
                val intent = Intent(this@AdminPanelActivity, AdminReportsActivity::class.java)
                startActivity(intent)
            }

            btnAddHost.setOnClickListener{
                val intent = Intent(this@AdminPanelActivity, AddHostActivity::class.java)
                startActivity(intent)
            }
            adminLogoutButton.setOnClickListener{
                showLogoutDialog(this@AdminPanelActivity)
            }

            homeBtn.setOnClickListener{
                val intent = Intent(this@AdminPanelActivity, VisitorLandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                this@AdminPanelActivity.finish()
            }
        }.root)
    }
}