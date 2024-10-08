package com.xenia.templekiosk.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val PREF_NAME = "UserSession"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_COMPANY_ID = "companyId"
    }


    fun saveUserDetails(userId: Int, userName: String, companyId: Int) {
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putInt(KEY_COMPANY_ID, companyId)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserName() != null
    }


    fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID,0)
    }

    private fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun getCompanyId(): Int {
        return sharedPreferences.getInt(KEY_COMPANY_ID, 0)
    }


    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}
