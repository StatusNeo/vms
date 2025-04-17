package com.android.visitormanagementsystem.utils

import android.content.Context
import android.content.SharedPreferences
import com.android.visitormanagementsystem.utils.Constants.HOST_NAME
import com.android.visitormanagementsystem.utils.Constants.KEY_Logged_InFrom
import com.android.visitormanagementsystem.utils.Constants.USER_MOBILE
import com.android.visitormanagementsystem.utils.Constants.USER_SESSION

object Prefs {

    fun customPreference(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    inline fun SharedPreferences.Editor.put(pair: Pair<String, Any>) {
        val key = pair.first
        val value = pair.second
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            else -> error("Only primitive types can be stored in SharedPreferences")
        }
    }

    var SharedPreferences.userMobileNo
        get() = getString(USER_MOBILE, "")
        set(value) {
            editMe {
                it.putString(USER_MOBILE, value)
            }
        }
    var SharedPreferences.hostName
        get() = getString(HOST_NAME, "")
        set(value) {
            editMe {
                it.putString(HOST_NAME, value)
            }
        }

    var SharedPreferences.LoggedInFrom
        get() = getString(KEY_Logged_InFrom, "")
        set(value) {
            editMe {
                it.putString(KEY_Logged_InFrom, value)
            }
        }

    var SharedPreferences.isLoggedIn
        get() = getBoolean(USER_SESSION, false)
        set(value) {
            editMe {
                //it.put(USER_PASSWORD to value)
                it.putBoolean(USER_SESSION, value)
            }
        }

    var SharedPreferences.clearValues
        get() = { }
        set(value) {
            editMe {
                /*it.remove(USER_ID)
                it.remove(USER_PASSWORD)*/
                it.clear()
            }
        }
}