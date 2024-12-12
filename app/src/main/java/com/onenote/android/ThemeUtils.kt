package com.onenote.android

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {
    private const val THEME_PREF = "theme_preferences"
    private const val IS_DARK_MODE = "is_dark_mode"

    fun setTheme(context: Context, isDarkMode: Boolean) {
        context.getSharedPreferences(THEME_PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(IS_DARK_MODE, isDarkMode)
            .apply()

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun isDarkMode(context: Context): Boolean {
        return context.getSharedPreferences(THEME_PREF, Context.MODE_PRIVATE)
            .getBoolean(IS_DARK_MODE, false)
    }
}
