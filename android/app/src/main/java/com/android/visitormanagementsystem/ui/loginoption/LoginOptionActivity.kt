package com.android.visitormanagementsystem.ui.loginoption

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.ui.aboutus.AboutUsActivity
import com.android.visitormanagementsystem.ui.adminlogin.AdminLoginActivity
import com.android.visitormanagementsystem.ui.host.hostlogin.HostLoginActivity
import com.android.visitormanagementsystem.ui.securitylogin.SecurityLoginActivity

class LoginOptionActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_options)

        var btn_security = findViewById<Button>(R.id.btn_security)
        var btn_admin_login = findViewById<Button>(R.id.btn_admin_login)
        var btn_host_login = findViewById<Button>(R.id.btn_host)
       var tvAboutVms= findViewById<TextView>(R.id.tv_about_vms)
        tvAboutVms.paintFlags = tvAboutVms.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        btn_security.setOnClickListener {
            val intent = Intent(this@LoginOptionActivity, SecurityLoginActivity::class.java)
            startActivity(intent)
        }
        btn_admin_login.setOnClickListener {
            val intent = Intent(this@LoginOptionActivity, AdminLoginActivity::class.java)
            startActivity(intent)
        }

        btn_host_login.setOnClickListener{
            val intent =
                Intent(this@LoginOptionActivity, HostLoginActivity::class.java)
            startActivity(intent)
        }
        tvAboutVms.setOnClickListener {
            val intent =
                Intent(this@LoginOptionActivity, AboutUsActivity::class.java)
            startActivity(intent)
        }


    }
}