package com.android.visitormanagementsystem.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.ui.loginoption.LoginOptionActivity
import com.android.visitormanagementsystem.utils.Prefs.clearValues
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun Context.toast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showLogoutDialog(context: Context) {
    val alertDialog = AlertDialog.Builder(context)
    alertDialog.apply {
        setIcon(R.drawable.logo)
        setTitle("Log Out?")
        setMessage("Are you sure want to log out?")
        setPositiveButton("Logout") { _, _ ->

            val prefs = Prefs.customPreference(context, Constants.LoggedIn_Pref)
            prefs.clearValues

            val intent = Intent(context, LoginOptionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
        setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
    }.create().show()
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

