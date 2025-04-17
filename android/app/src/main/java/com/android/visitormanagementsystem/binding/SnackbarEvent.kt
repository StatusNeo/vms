package com.android.visitormanagementsystem.binding

import androidx.annotation.Keep

@Keep
data class SnackbarEvent(
    val message: Message,
    override var consumed: Boolean = false
) : BaseEvent() {

    constructor(
        messageId: Int,
        error: Throwable? = null
    ) : this(Message(messageId, error))

    companion object {
        val NONE = SnackbarEvent(-1)
    }
}

@Keep
data class SnackbarEventMessage(
    val message: MessageString,
    override var consumed: Boolean = false
) : BaseEvent() {

    constructor(
        messageId: String,
        error: Throwable? = null
    ) : this(MessageString(messageId, error))

    companion object {
        val NONE = SnackbarEventMessage("")
    }
}
