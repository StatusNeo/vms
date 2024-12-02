package com.android.visitormanagementsystem.ui.securitylogin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.SecurityLoginBinding
import com.android.visitormanagementsystem.ui.securitypanel.SecurityPanelActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState


class SecurityLoginActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    var securityLoginViewState = ProgressBarViewState()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SecurityLoginBinding.inflate(layoutInflater).apply {

            viewState = securityLoginViewState
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
                } else {
                    securityLoginViewState.progressbarEvent = true
                    db.collection(Constants.SECURITY)
                        .get()
                        .addOnSuccessListener { result ->
                            securityLoginViewState.progressbarEvent = false
                            for (document in result) {
                                if (etUserName.text.toString() == document.data["userName"] && etPassword.text.toString() == document.data["password"]){
                                    val intent =
                                        Intent(this@SecurityLoginActivity, SecurityPanelActivity::class.java)
                                    startActivity(intent)
                                    this@SecurityLoginActivity.finish()
                                }else{
                                    showToast(R.string.msg_valid_creds)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            securityLoginViewState.progressbarEvent = false
                            Log.w("users error>>", "Error getting documents.", exception)
                        }
                }
            }
            backBtn.setOnClickListener {
                this@SecurityLoginActivity.finish()
            }
        }.root)
    }

    private fun showToast(msg:Int){
        val toast: Toast = Toast.makeText(this@SecurityLoginActivity, msg, Toast.LENGTH_SHORT)
        toast.show()
    }
}