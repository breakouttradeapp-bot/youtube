package com.aitube.seogenerator.utils

import android.content.Context
import android.content.SharedPreferences
import com.aitube.seogenerator.models.HistoryItem
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getGenerationCount(): Int = prefs.getInt(Constants.KEY_GEN_COUNT, 0)

    fun incrementCount() {
        prefs.edit().putInt(Constants.KEY_GEN_COUNT, getGenerationCount() + 1).apply()
    }

    fun resetCount() {
        prefs.edit().putInt(Constants.KEY_GEN_COUNT, 0).apply()
    }

    fun getUnlockUntil(): Long = prefs.getLong(Constants.KEY_UNLOCK_UNTIL, 0L)

    fun setUnlockUntil(time: Long) {
        prefs.edit().putLong(Constants.KEY_UNLOCK_UNTIL, time).apply()
    }

    fun isUnlocked(): Boolean = System.currentTimeMillis() < getUnlockUntil()

    fun isDarkMode(): Boolean = prefs.getBoolean(Constants.KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_DARK_MODE, enabled).apply()
    }

    fun saveHistory(item: HistoryItem) {
        try {
            val list = getHistory().toMutableList()
            list.add(0, item)
            if (list.size > 50) list.subList(50, list.size).clear()
            prefs.edit().putString(Constants.KEY_HISTORY, gson.toJson(list)).apply()
        } catch (e: Exception) {
            // History save failing must not crash the app
        }
    }

    fun getHistory(): List<HistoryItem> {
        return try {
            val json = prefs.getString(Constants.KEY_HISTORY, null) ?: return emptyList()
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            gson.fromJson<List<HistoryItem>>(json, type) ?: emptyList()
        } catch (e: JsonSyntaxException) {
            // Corrupted history — clear it and start fresh
            prefs.edit().remove(Constants.KEY_HISTORY).apply()
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearHistory() {
        prefs.edit().remove(Constants.KEY_HISTORY).apply()
    }
}
