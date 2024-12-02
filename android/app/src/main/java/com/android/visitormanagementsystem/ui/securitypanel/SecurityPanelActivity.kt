package com.android.visitormanagementsystem.ui.securitypanel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.ui.verifyvisitor.VerifyVisitorActivity
import com.android.visitormanagementsystem.ui.visitorList.VisitorListActivity
import com.android.visitormanagementsystem.ui.visitorotp.VisitorsOTPFirbActivity
import com.android.visitormanagementsystem.utils.showLogoutDialog

class SecurityPanelActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.security_panel)
        var btn_verify = findViewById<Button>(R.id.btn_verify)
        var btn_view = findViewById<Button>(R.id.btn_view)
        var btn_manage = findViewById<Button>(R.id.btn_manage)
        var btnBack = findViewById<ImageView>(R.id.backBtn)
        var securityLogoutButton = findViewById<FloatingActionButton>(R.id.securityLogoutButton)

        securityLogoutButton.setOnClickListener{
            showLogoutDialog(this@SecurityPanelActivity)
        }

        btn_verify.setOnClickListener {
            val intent = Intent(this@SecurityPanelActivity, VerifyVisitorActivity::class.java)
            startActivity(intent)
        }

        btn_view.setOnClickListener{
            val intent = Intent(this@SecurityPanelActivity, VisitorListActivity::class.java)
            startActivity(intent)
        }
       btn_manage.setOnClickListener{
            val intent = Intent(this@SecurityPanelActivity, VisitorsOTPFirbActivity::class.java)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            this@SecurityPanelActivity.finish()
        }
    }


}