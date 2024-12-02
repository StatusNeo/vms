package com.android.visitormanagementsystem.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.visitormanagementsystem.HostListItemBinding
import com.android.visitormanagementsystem.ui.adminpanel.GetAllHostUiModel
import com.android.visitormanagementsystem.ui.interfaces.OnHostClickInterface

class GetAllHostAdapter (var items: ArrayList<GetAllHostUiModel>,
                         private val itemClickListener: OnHostClickInterface) :
    RecyclerView.Adapter<GetAllHostAdapter.ViewHolder>()
{

    fun setUserList(updatedUserList: List<GetAllHostUiModel>) {
        items.clear()
        items.addAll(updatedUserList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HostListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.binding.nextImage.setOnClickListener {
            itemClickListener.onHostClick(
                items[position]
            )
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: HostListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reportsListUiModel: GetAllHostUiModel) {
            with(binding) {
                data = reportsListUiModel
                executePendingBindings()
            }
        }
    }
}