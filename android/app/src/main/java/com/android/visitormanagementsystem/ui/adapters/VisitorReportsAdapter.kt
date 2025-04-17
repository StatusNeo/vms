package com.android.visitormanagementsystem.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.visitormanagementsystem.HostVisitorReportsItemBinding
import com.android.visitormanagementsystem.ui.host.visitorreports.VisitorReportsUIModel

class VisitorReportsAdapter ( var items: ArrayList<VisitorReportsUIModel>) :
    RecyclerView.Adapter<VisitorReportsAdapter.ViewHolder>()
{
    fun setUserList(updatedUserList: List<VisitorReportsUIModel>) {
        items.clear()
        items.addAll(updatedUserList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HostVisitorReportsItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: HostVisitorReportsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reportUiModel: VisitorReportsUIModel) {
            with(binding) {
                data = reportUiModel
                executePendingBindings()
            }
        }
    }
}