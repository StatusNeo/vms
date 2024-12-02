package com.android.visitormanagementsystem.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.leadschool.teacherapp.base.ui.binding.adapter.BindableAdapter
import com.android.visitormanagementsystem.VisitorItemBinding
import com.android.visitormanagementsystem.ui.visitorList.VisitorListViewModel
import com.android.visitormanagementsystem.ui.visitorList.VisitorListUiModel
import com.android.visitormanagementsystem.ui.interfaces.OnVisitorClickInterface
import com.android.visitormanagementsystem.utils.diffCallback

class VisitorListAdapter(
    private val visitorViewModel: VisitorListViewModel,
    private val itemClickListener: OnVisitorClickInterface
) : RecyclerView.Adapter<VisitorListAdapter.ViewHolder>(),
    BindableAdapter<VisitorListUiModel> {

     override var items: List<VisitorListUiModel>  by diffCallback(emptyList()) { o, n ->
         o.id.toString() + o.visitorMobileNo.toString() + o.visitorName == n.id.toString() + n.visitorMobileNo.toString() + n.visitorName
     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            VisitorItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(visitorViewModel, items[position], position)
        holder.binding.nextImage.setOnClickListener {
            itemClickListener.onVisitorClick(
                items[position], position
            )
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: VisitorItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vm: VisitorListViewModel, visitorListUiModel: VisitorListUiModel, pos: Int) {
            with(binding) {
                viewModel = vm
                data = visitorListUiModel
                itemPosition = pos
                executePendingBindings()
            }
        }
    }
}