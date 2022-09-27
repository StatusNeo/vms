package com.android.visitormanagementsystem.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.leadschool.teacherapp.base.ui.binding.adapter.BindableAdapter
import com.android.visitormanagementsystem.PlannedVisitItemBinding
import com.android.visitormanagementsystem.ui.plannedvisit.PlannedVisitUiModel
import com.android.visitormanagementsystem.utils.diffCallback

class PlannedVisitAdapter :
    RecyclerView.Adapter<PlannedVisitAdapter.ViewHolder>(), BindableAdapter<PlannedVisitUiModel> {

    override var items: List<PlannedVisitUiModel>  by diffCallback(emptyList()) { o, n ->
        o.id.toString() + o.hostMobileNo.toString() + o.hostMobileNo == n.id.toString() + n.hostMobileNo.toString() + n.hostName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PlannedVisitItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(var plannedVisitItemBinding: PlannedVisitItemBinding) :
        RecyclerView.ViewHolder(plannedVisitItemBinding.root) {
        fun bind(plannedVisitUiModel: PlannedVisitUiModel) {
            with(plannedVisitItemBinding) {
                data = plannedVisitUiModel
                executePendingBindings()
            }
        }
    }
}