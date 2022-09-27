package com.android.visitormanagementsystem.ui.host.hostlogin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.databinding.ActivityHostLoginBinding
import com.android.visitormanagementsystem.ui.host.hostpanel.HostPanelActivity
import com.android.visitormanagementsystem.utils.Constants
import com.android.visitormanagementsystem.utils.ProgressBarViewState
import com.android.visitormanagementsystem.utils.toast
import timber.log.Timber

class HostLoginActivity : AppCompatActivity() {
    private lateinit var hostLoginViewModel: HostLoginViewModel
    var progressViewState = ProgressBarViewState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hostLoginViewModel = ViewModelProvider(this)[HostLoginViewModel::class.java]
        setContentView(ActivityHostLoginBinding.inflate(layoutInflater).apply {
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
                } else {
                    progressViewState.progressbarEvent = true
                    val myDB = FirebaseFirestore.getInstance().collection(Constants.EMPLOYEE_LIST)
                    val query1: com.google.firebase.firestore.Query =
                        myDB.whereEqualTo(Constants.USER_NAME, etUserName.text.toString())
                    query1.get().addOnSuccessListener { result ->
                            progressViewState.progressbarEvent = false
                        if(!result.isEmpty) {
                            for (document in result) {
                                if (etUserName.text.toString() == document.data["userName"] && etPassword.text.toString() == document.data["password"]) {
                                 /*   hostLoginViewModel.getHostDetails(Prefs.customPreference(this@HostLoginActivity, LoggedIn_Pref),
                                        document.data["hostMobileNo"] as String)*/

                                    val intent = Intent(this@HostLoginActivity, HostPanelActivity::class.java)
                                    startActivity(intent)
                                    this@HostLoginActivity.finish()
                                } else {
                                    toast(R.string.msg_valid_creds)
                                }
                            }
                        }else{
                            toast(R.string.msg_valid_creds)
                        }
                        }.addOnFailureListener { exception ->
                            progressViewState.progressbarEvent = false
                            toast(R.string.msg_something_went)
                            Timber.tag("HostLogin error>>")
                                .w(exception, "Error getting documents.")
                        }
                }
            }
            backBtn.setOnClickListener {
                this@HostLoginActivity.finish()
            }
        }.root)
    }
}