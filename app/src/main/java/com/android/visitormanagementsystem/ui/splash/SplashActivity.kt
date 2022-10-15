package com.android.visitormanagementsystem.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.ui.interfaces.SplashNavigator
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), SplashNavigator {
    private lateinit var splashViewModel: SplashViewModel
    private lateinit var listener : SplashNavigator

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        listener = this
        initViewModel()
    }

    private fun initViewModel() {
        splashViewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        splashViewModel.initSplashScreen(listener)

    }

    override fun openLoginActivity() {
        val intent = Intent(this@SplashActivity, VisitorLandingActivity::class.java)
        startActivity(intent)
        this@SplashActivity.finish()
    }

    override fun openMainActivity() {
        TODO("Not yet implemented")
    }

}