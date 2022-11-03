package com.android.visitormanagementsystem.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.ui.adminpanel.AdminPanelActivity
import com.android.visitormanagementsystem.ui.host.hostreports.HostReportsActivity
import com.android.visitormanagementsystem.ui.interfaces.SplashNavigator
import com.android.visitormanagementsystem.ui.visitorList.VisitorListActivity
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.Prefs
import com.android.visitormanagementsystem.utils.Prefs.LoggedInFrom
import com.android.visitormanagementsystem.utils.Prefs.userMobileNo

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), SplashNavigator {
    private lateinit var splashViewModel: SplashViewModel
    private lateinit var listener : SplashNavigator
    lateinit var prefs : SharedPreferences


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        listener = this
        initViewModel()
    }

    private fun initViewModel() {
        prefs = Prefs.customPreference(this@SplashActivity, Constants.LoggedIn_Pref)
        splashViewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        splashViewModel.initSplashScreen(listener, prefs.LoggedInFrom.toString()
        )
    }

    override fun openLoginActivity() {
        val intent = Intent(this@SplashActivity, VisitorLandingActivity::class.java)
        startActivity(intent)
        this@SplashActivity.finish()
    }

    override fun openHostActivity() {
        val intent = Intent(this@SplashActivity, HostReportsActivity::class.java)
        intent.putExtra("mobile", prefs.userMobileNo)
        startActivity(intent)
        this@SplashActivity.finish()
    }

    override fun openAdminActivity() {
        startActivity(Intent(this@SplashActivity, AdminPanelActivity::class.java))
        this@SplashActivity.finish()
    }

    override fun openSecurityActivity() {
        startActivity(Intent(this@SplashActivity, VisitorListActivity::class.java))
        this@SplashActivity.finish()
    }
}