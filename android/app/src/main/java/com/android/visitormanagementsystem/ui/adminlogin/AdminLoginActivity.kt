package com.android.visitormanagementsystem.ui.adminlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.AdminLoginBinding
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.ui.adminpanel.AdminPanelActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState

class AdminLoginActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    var progressViewState = ProgressBarViewState()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(AdminLoginBinding.inflate(layoutInflater).apply {
            viewState = progressViewState
            etUserName.requestFocus()
            btnNext.setOnClickListener {
                if(etUserName.text.isBlank() && etPassword.text.isBlank()) {
                    etUserName.error = getString(R.string.please_enter_user_name)
                    etPassword.error = getString(R.string.please_enter_password)
                } else if(etUserName.text.isBlank()) {
                    etUserName.error = getString(R.string.please_enter_user_name)

                } else if(etPassword.text.isBlank()) {
                    etPassword.requestFocus()
                    etPassword.error = getString(R.string.please_enter_password)
                } /*else if( !(etUserName.text.toString() == "Admin" && etPassword.text.toString() == "Pass@123") ){
                    showToast(R.string.msg_valid_creds)
                }*/
                else {
                    progressViewState.progressbarEvent = true
                    db.collection(Constants.ADMIN)
                        .get()
                        .addOnSuccessListener { result ->
                            progressViewState.progressbarEvent = false
                            for (document in result) {
                                if (etUserName.text.toString() == document.data["userName"] && etPassword.text.toString() == document.data["password"]){
                                    startActivity( Intent(this@AdminLoginActivity, AdminPanelActivity::class.java))
                                    this@AdminLoginActivity.finish()
                                }else{
                                    showToast(R.string.msg_valid_creds)
                                }
                            }
                        }.addOnFailureListener { exception ->
                            progressViewState.progressbarEvent = false
                            showToast(R.string.msg_something_went)
                            Log.w("Admin error>>", "Error getting documents.", exception)
                        }
                }
            }
            backBtn.setOnClickListener {
                this@AdminLoginActivity.finish()
            }
        }.root)
    }

    private fun showToast(msg:Int){
        val toast: Toast = Toast.makeText(this@AdminLoginActivity, msg, Toast.LENGTH_SHORT)
        toast.show()
    }
}