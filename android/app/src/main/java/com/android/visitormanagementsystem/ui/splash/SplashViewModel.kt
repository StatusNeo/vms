package com.android.visitormanagementsystem.ui.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.visitormanagementsystem.ui.interfaces.SplashNavigator
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.Prefs.LoggedInFrom
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var splashNavigator : SplashNavigator


    fun initSplashScreen(listener: SplashNavigator, loggedInFrom : String) {
        splashNavigator = listener
        viewModelScope.launch {
            delay(2000)
            decideNextActivity(loggedInFrom)
        }
    }

    private fun decideNextActivity(loggedInFrom: String) {

        if(loggedInFrom == Constants.VALUE_HOST_LOGIN){
            splashNavigator.openHostActivity()
        }else if(loggedInFrom == Constants.VALUE_ADMIN_LOGIN){
            splashNavigator.openAdminActivity()
        }else if(loggedInFrom == Constants.VALUE_SECURITY_LOGIN){
            splashNavigator.openSecurityActivity()
        }else{
            splashNavigator.openLoginActivity()
        }
    }
}
