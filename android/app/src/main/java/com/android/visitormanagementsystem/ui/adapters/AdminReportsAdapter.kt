package com.android.visitormanagementsystem.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.visitormanagementsystem.ReportsItemBinding
import com.android.visitormanagementsystem.ui.adminreports.AdminReportsUiModel
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorReportClickInterface

class AdminReportsAdapter(var items: ArrayList<AdminReportsUiModel>,
                          private val itemClickListener: OnVisitorReportClickInterface) :
    RecyclerView.Adapter<AdminReportsAdapter.ViewHolder>()
{

    fun setUserList(updatedUserList: List<AdminReportsUiModel>) {
        items.clear()
        items.addAll(updatedUserList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ReportsItemBinding.inflate(
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

    class ViewHolder(val binding: ReportsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reportsListUiModel: AdminReportsUiModel) {
            with(binding) {
                data = reportsListUiModel
                executePendingBindings()
            }
        }
    }
}