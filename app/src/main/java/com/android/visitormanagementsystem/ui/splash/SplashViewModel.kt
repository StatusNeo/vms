package com.android.visitormanagementsystem.ui.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.visitormanagementsystem.ui.interfaces.SplashNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var splashNavigator : SplashNavigator


    fun initSplashScreen(listener: SplashNavigator) {
        splashNavigator = listener
        viewModelScope.launch {
            delay(2000)
            decideNextActivity()
        }
    }

    private fun decideNextActivity() {
        //check from Shared pref for Logged in
//        if(getDataManager().getCurrentUserLoggedInMode() === DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT.getType()) {
//            getNavigator().openLoginActivity()
//        } else {
//            getNavigator().openMainActivity()
//        }

        splashNavigator.openLoginActivity()
    }
}
