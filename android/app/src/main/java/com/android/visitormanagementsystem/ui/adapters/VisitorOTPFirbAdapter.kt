package com.android.visitormanagementsystem.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.leadschool.teacherapp.base.ui.binding.adapter.BindableAdapter
import com.android.visitormanagementsystem.R
import com.android.visitormanagementsystem.VisitorOTPFirbItemBinding
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorOTPFirbClickInterface
import com.android.visitormanagementsystem.ui.visitorotp.VisitorsOTPFirbUIModel
import com.android.visitormanagementsystem.utils.diffCallback

class VisitorOTPFirbAdapter (
    private val itemClickListener: OnVisitorOTPFirbClickInterface
) : RecyclerView.Adapter<VisitorOTPFirbAdapter.ViewHolder>(),
    BindableAdapter<VisitorsOTPFirbUIModel> {

    override var items: List<VisitorsOTPFirbUIModel> by diffCallback(emptyList()) { o, n ->
        o.id.toString() + o.visitorMobileNo.toString() + o.otpNo.toString() == n.id.toString() + n.visitorMobileNo.toString() + n.otpNo.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            VisitorOTPFirbItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }



    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: VisitorOTPFirbItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(visitorOTPUiModel: VisitorsOTPFirbUIModel, pos: Int) {
            with(binding) {
                data = visitorOTPUiModel
                itemPosition = pos
                executePendingBindings()
            }
        }
    }

    override fun onBindViewHolder(holder: VisitorOTPFirbAdapter.ViewHolder, position: Int) {
        holder.bind(items[position], position)
        holder.binding.btnOtpVerify.setOnClickListener {
            Log.d("otp>>>",holder.binding.etOtp.text.toString())
            itemClickListener.onOTPVerifyClick(
                items[position], holder.binding.etOtp.text.toString()
            )
        }
        holder.binding.btnOtpResend.setOnClickListener {
            holder.binding.btnOtpVerify.setBackgroundResource(R.drawable.rounded_corner)
            holder.binding.btnOtpVerify.isEnabled = true
            holder.binding.btnOtpVerify.isClickable = true
            itemClickListener.onOTPResendClick(
                items[position], position
            )
        }

    }


}