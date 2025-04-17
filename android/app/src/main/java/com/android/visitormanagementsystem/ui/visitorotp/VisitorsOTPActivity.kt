package com.android.visitormanagementsystem.ui.visitorotp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.visitormanagementsystem.databinding.ActivityVisitorsOtpBinding
import com.android.visitormanagementsystem.ui.adapters.VisitorOTPAdapter
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorOTPClickInterface
import com.android.visitormanagementsystem.ui.registervisitor.RegisterVisitorActivity

class VisitorsOTPActivity : AppCompatActivity() {

    lateinit var viewModelOtp : VisitorOtpViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelOtp = ViewModelProvider(this)[VisitorOtpViewModel::class.java]
        setContentView(ActivityVisitorsOtpBinding.inflate(layoutInflater).apply {
            viewModel = viewModelOtp
            viewState = viewModelOtp.viewState
            with(visitorOTPRecyclerView){
                adapter = VisitorOTPAdapter(object : OnVisitorOTPClickInterface{
                    override fun onOTPVerifyClick(uiModel: VisitorOTPUiModel, pos: Int) {
                        showToast("OTP Verified")
                        val intent = Intent(this@VisitorsOTPActivity, RegisterVisitorActivity::class.java)
                        intent.putExtra("mobile",uiModel.visitorMobileNo.toString())
                        startActivity(intent)
                    }
                    override fun onOTPResendClick(uiModel: VisitorOTPUiModel, pos: Int) {
                        showToast("OTP has been Resent")
                    }
                })
            }

            backBtn.setOnClickListener {
                this@VisitorsOTPActivity.finish()
            }

        }.root)
    }

    private fun showToast(msg:String){
        val toast: Toast = Toast.makeText(this@VisitorsOTPActivity, msg, Toast.LENGTH_SHORT)
        toast.show()
    }
}