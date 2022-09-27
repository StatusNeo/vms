package com.android.visitormanagementsystem.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leadschool.teacherapp.base.ui.binding.adapter.BindableAdapter

/**
 * Set items into [BindableAdapter], throws [IllegalStateException] if [RecyclerView.Adapter] does not implements
 * [BindableAdapter].
 */
@BindingAdapter("items")
fun <T> RecyclerView.setItems(items: List<T>) {
    val tempAdapter = adapter
    if (tempAdapter is BindableAdapter<*>) {
        @Suppress("UNCHECKED_CAST")
        (tempAdapter as BindableAdapter<T>).items = items
    } else {
        throw IllegalStateException("Your adapter should implement BindableAdapter")
    }
}

