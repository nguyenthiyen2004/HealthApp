package com.example.healthapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {

    private const val PREFS_NAME = "secure_prefs"

    // Hàm tạo EncryptedSharedPreferences
    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        // Tạo MasterKey
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Tạo EncryptedSharedPreferences
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Hàm lưu dữ liệu
    fun saveData(context: Context, key: String, value: String) {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        sharedPreferences.edit().putString(key, value).apply()
    }

    // Hàm lấy dữ liệu
    fun getData(context: Context, key: String): String? {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        return sharedPreferences.getString(key, null)
    }
}