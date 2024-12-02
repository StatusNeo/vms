package com.leadschool.teacherapp.base.domain.exception

/**
 * Wrapper for [error][Throwable] with a [message][ErrorMessage].
 */
class ExceptionWrapper(
        val errorMessage: ErrorMessage,
        cause: Throwable
) : Exception("${cause.message} ${errorMessage.message}", cause) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExceptionWrapper

        if (errorMessage != other.errorMessage) return false

        return true
    }

    override fun hashCode(): Int {
        return errorMessage.hashCode()
    }

    override fun toString(): String {
        return "ExceptionWrapper(errorMessage=$errorMessage)"
    }

}
