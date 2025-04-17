package com.android.visitormanagementsystem.binding

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.leadschool.teacherapp.base.domain.exception.ExceptionWrapper


/**
 * Data class to represent a [message resource][messageId] with a optional [failure][error].
 */
@Keep
data class Message(
    @StringRes val messageId: Int,
    val error: Throwable? = null
)

/**
 * To resolve a message as a [String].
 */
fun Message.resolve(context: Context): String {
    return if (error is ExceptionWrapper) {
        error.errorMessage.message
    } else {
        context.getString(messageId)
    }
}

@Keep
data class MessageString(
    val messageId: String,
    val error: Throwable? = null
)

/**
 * To resolve a message as a [String].
 */
fun MessageString.resolve(): String {
    return if (error is ExceptionWrapper) {
        error.errorMessage.message
    } else {
        messageId
    }
}


