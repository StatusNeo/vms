package com.android.visitormanagementsystem.utils

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Creates [BindDelegate] with BR field ID and initial value.
 */
fun <T> bind(
    fieldId: Int,
    value: T,
    onChange: (old: T, new: T) -> Unit = { _, _ -> }
): BindDelegate<T> = BindDelegate(fieldId, value, onChange)

/**
 * Delegate for [Bindable] properties, which automatically notifies listeners that property has changed.
 */
class BindDelegate<T>(
    private val fieldId: Int,
    private var value: T,
    private val onChange: (old: T, new: T) -> Unit
) : ReadWriteProperty<BaseObservable, T> {

    override fun getValue(thisRef: BaseObservable, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: BaseObservable, property: KProperty<*>, value: T) {
        val oldValue = this.value
        if (oldValue == value) return

        this.value = value
        onChange(oldValue, value)
        thisRef.notifyPropertyChanged(fieldId)
    }
}
