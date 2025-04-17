package com.android.visitormanagementsystem.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.visitormanagementsystem.BlackListedItemBinding
import com.android.visitormanagementsystem.ui.blacklistedvisitor.BlackListedVisitorUiModel

class BlacklistedVisitorAdapter( var items: ArrayList<BlackListedVisitorUiModel>) : RecyclerView.Adapter<BlacklistedVisitorAdapter.ViewHolder>()
{

    fun setUserList(updatedUserList: List<BlackListedVisitorUiModel>) {
        items.clear()
        items.addAll(updatedUserList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BlackListedItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: BlackListedItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reportsListUiModel: BlackListedVisitorUiModel) {
            with(binding) {
                data = reportsListUiModel
                executePendingBindings()
            }
        }
    }
}