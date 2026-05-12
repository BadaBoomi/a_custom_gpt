package com.badaboomi.acustomgpt.util

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat

object AccountUtils {
    /**
     * Versucht, die primäre Google-Mail-Adresse des Users zu ermitteln.
     * Erfordert android.permission.GET_ACCOUNTS
     */
    @JvmStatic
    @RequiresPermission("android.permission.GET_ACCOUNTS")
    fun getGoogleAccountEmail(context: Context): String? {
        if (com.badaboomi.acustomgpt.tools.ToolConfig.logLevel == "DEBUG") {
            android.util.Log.d("AccountUtils", "Starte getGoogleAccountEmail")
        }
        val accountManager = ContextCompat.getSystemService(context, AccountManager::class.java)
        if (accountManager == null) {
            if (com.badaboomi.acustomgpt.tools.ToolConfig.logLevel == "DEBUG") {
                android.util.Log.d("AccountUtils", "AccountManager ist null")
            }
            return null
        }
        val accounts: Array<Account> = accountManager.getAccountsByType("com.google")
        if (com.badaboomi.acustomgpt.tools.ToolConfig.logLevel == "DEBUG") {
            android.util.Log.d("AccountUtils", "Gefundene Konten: ${accounts.joinToString { it.name }}")
        }
        val email = accounts.firstOrNull()?.name
        if (com.badaboomi.acustomgpt.tools.ToolConfig.logLevel == "DEBUG") {
            android.util.Log.d("AccountUtils", "Ermittelte E-Mail: $email")
        }
        return email
    }
}
