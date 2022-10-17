package com.android.visitormanagementsystem.ui.adapters

import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.binding.SnackbarEvent
import com.android.visitormanagementsystem.binding.consume
import com.android.visitormanagementsystem.binding.resolve

@BindingAdapter("viewVisibility")
fun View.setViewVisibility(viewVisibility: Boolean) {
    visibility = if(viewVisibility) {
        VISIBLE
    } else {
        GONE
    }
}

@BindingAdapter("loadImage")
fun ShapeableImageView.loadImage(url: String) {
    Picasso.get().load(url).placeholder(R.drawable.profile_icon).into(this)
}

@BindingAdapter("setBatchNumber")
fun TextView.setBatchNumber(batchNumber: String){
    text = context.getString(R.string.batch_number, batchNumber)
}

@BindingAdapter("setMobileNo")
fun TextView.setMobileNo(mobileNo: String){
    text = context.getString(R.string.mobile_number, mobileNo)
}

@BindingAdapter("setOTPNo")
fun TextView.setOTPNo(otpNo: Int){
    text = context.getString(R.string.otp_number, otpNo)
}

@BindingAdapter("setDate")
fun TextView.setDate(date: String){
    text = context.getString(R.string.date_string, date)
}

@BindingAdapter("setTime")
fun TextView.setTime(time: String){
    text = context.getString(R.string.time_string, time)
}
@BindingAdapter("setInTime")
fun TextView.setInTime(time: String){
    text = context.getString(R.string.in_time_string, time)
}
@BindingAdapter("setOutTime")
fun TextView.setOutTime(time: String){
    text = context.getString(R.string.out_time_string, time)
}

@BindingAdapter("setPurpose")
fun TextView.setPurpose(purpose: String){
    text = context.getString(R.string.purpose_string, purpose)
}
@BindingAdapter("setBlackListedDate")
fun TextView.setBlackListedDate(date: String){
    text = context.getString(R.string.date_string, date)
}

@BindingAdapter("setHostMobileNo")
fun TextView.setHostMobileNo(mobileNo: String){
    text = context.getString(R.string.host_mobile_number, mobileNo)
}

@BindingAdapter("setHostName")
fun TextView.setHostName(name: String){
    text = context.getString(R.string.host_name, name)
}

@BindingAdapter("error")
fun setError(editText: EditText, strOrResId: Any?) {
    if(strOrResId is Int) {
        editText.error = editText.context.getString((strOrResId as Int?)!!)
    } else {
        editText.error = strOrResId as String?
    }
}

@BindingAdapter("passwordValidator")
fun passwordValidator(editText: EditText, password: String?) {
    // ignore infinite loops
    val minimumLength = 5
    if(TextUtils.isEmpty(password)) {
        editText.error = null
        return
    }
    if(editText.text.toString().length < minimumLength) {
        editText.error = "Password must be minimum $minimumLength length"
    } else editText.error = null
}

@BindingAdapter("snackbar")
fun View.showSnackbar(event: SnackbarEvent) {
    if (event != SnackbarEvent.NONE) {
        event.consume {
            val message = it.message.resolve(context)
            val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)

            val snackbarView = snackbar.view
            val textView =
                snackbarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
            textView.maxLines = 2
            snackbar.show()
        }
    }
}

@BindingAdapter("downloadStatusViewVisibility")
fun ProgressBar.downloadStatusViewVisibility(flag: Boolean) {
    if(flag){
        setViewVisibility(true)
    }else{
        setViewVisibility(false)
    }
}