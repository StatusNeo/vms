package com.android.visitormanagementsystem.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.visitormanagementsystem.HostReportsItemBinding
import com.android.visitormanagementsystem.ui.adapters.HostReportAdapter.ViewHolder
import com.android.visitormanagementsystem.ui.host.hostreports.HostReportUiModel
import com.android.visitormanagementsystem.ui.interfaces.OnHostReportClickInterface
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorReportClickInterface

class HostReportAdapter( var items: ArrayList<HostReportUiModel>,
                         private val itemClickListener: OnHostReportClickInterface
) :
    RecyclerView.Adapter<ViewHolder>()
{
    fun setUserList(updatedUserList: List<HostReportUiModel>) {
        items.clear()
        items.addAll(updatedUserList)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HostReportsItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.binding.nextImage.setOnClickListener {
            itemClickListener.onVisitorClick(
                items[position], position
            )
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: HostReportsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reportUiModel: HostReportUiModel) {
            with(binding) {
                data = reportUiModel
                executePendingBindings()
            }
        }
    }
}