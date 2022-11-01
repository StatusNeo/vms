package com.android.visitormanagementsystem.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.LogoutDialogBinding
import com.android.visitormanagementsystem.ui.visitorlanding.VisitorLandingActivity
import java.text.SimpleDateFormat
import java.util.*

fun Context.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showLogoutDialog(context: Context) {
    val dialogBinding: LogoutDialogBinding? =
        DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.logout_dialog,
            null,
            false
        )

    val customDialog = AlertDialog.Builder(context, 0).create()
    customDialog.apply {
        setView(dialogBinding?.root)
        setCancelable(false)
    }.show()

    dialogBinding?.tv2Cancel?.setOnClickListener {
        customDialog.dismiss()
    }

    dialogBinding?.tv2Yes?.setOnClickListener {
        val prefs = Prefs.customPreference(context, Constants.LoggedIn_Pref)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(context, VisitorLandingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
        customDialog.dismiss()
    }
}

fun getCurrentDate() : String{
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
    return sdf.format(calendar.getTime())
}
fun getCurrentTime() : String{
    var  date =  Date(System.currentTimeMillis());
    var dateFormat =  SimpleDateFormat("hh:mm aa",
        Locale.ENGLISH)
     return dateFormat.format(date)
}

