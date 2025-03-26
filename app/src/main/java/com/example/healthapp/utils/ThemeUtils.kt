package com.example.healthapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    // Lưu trạng thái chế độ sáng/tối
    fun saveThemeMode(context: Context, isDarkMode: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(KEY_THEME_MODE, isDarkMode).apply()
    }

    // Lấy trạng thái chế độ sáng/tối
    fun isDarkMode(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_THEME_MODE, false) // Mặc định là chế độ sáng
    }

    // Áp dụng chế độ sáng/tối
    fun applyTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}