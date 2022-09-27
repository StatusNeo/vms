package com.android.visitormanagementsystem.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.leadschool.teacherapp.base.ui.binding.adapter.BindableAdapter
import com.android.visitormanagementsystem.VisitorOTPItemBinding
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorOTPClickInterface
import com.android.visitormanagementsystem.ui.visitorotp.VisitorOTPUiModel
import com.android.visitormanagementsystem.utils.diffCallback

class VisitorOTPAdapter(
    private val itemClickListener: OnVisitorOTPClickInterface
) : RecyclerView.Adapter<VisitorOTPAdapter.ViewHolder>(),
    BindableAdapter<VisitorOTPUiModel> {

     override var items: List<VisitorOTPUiModel> by diffCallback(emptyList()) { o, n ->
         o.id.toString() + o.visitorMobileNo.toString() + o.otpNo.toString() == n.id.toString() + n.visitorMobileNo.toString() + n.otpNo.toString()
     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            VisitorOTPItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
        holder.binding.btnOtpVerify.setOnClickListener {
            itemClickListener.onOTPVerifyClick(
                items[position], position
            )
        }
        holder.binding.btnOtpResend.setOnClickListener {
            itemClickListener.onOTPResendClick(
                items[position], position
            )
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: VisitorOTPItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(visitorOTPUiModel: VisitorOTPUiModel, pos: Int) {
            with(binding) {
                data = visitorOTPUiModel
                itemPosition = pos
                executePendingBindings()
            }
        }
    }
}