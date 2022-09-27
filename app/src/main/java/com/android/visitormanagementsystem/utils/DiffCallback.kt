package com.android.visitormanagementsystem.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates.observable

fun <T> RecyclerView.Adapter<*>.diffCallback(initialValue: List<T>, compare: (T, T) -> Boolean) =
    observable(initialValue) { _, old, new ->
        val diffCallBack = DiffCallBack(old, new, compare)
        val result = DiffUtil.calculateDiff(diffCallBack)
        result.dispatchUpdatesTo(this)
    }

class DiffCallBack<T>(
    private val old: List<T>,
    private val new: List<T>,
    private val compare: (T, T) -> Boolean
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return compare(old[oldItemPosition], new[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }

    override fun getOldListSize():Int{
        return old.size
    }

    override fun getNewListSize() : Int{
        return new.size
    }
}
