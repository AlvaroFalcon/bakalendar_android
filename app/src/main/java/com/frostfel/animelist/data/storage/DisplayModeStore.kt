package com.frostfel.animelist.data.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Cards or compact list, shared by both tabs and remembered between launches. */
@Singleton
class DisplayModeStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("display_mode", Context.MODE_PRIVATE)
    private val _listMode = MutableStateFlow(prefs.getBoolean(LIST_MODE_KEY, false))
    val listMode: StateFlow<Boolean> = _listMode

    fun toggle() {
        val value = !_listMode.value
        _listMode.value = value
        prefs.edit().putBoolean(LIST_MODE_KEY, value).apply()
    }

    private companion object {
        const val LIST_MODE_KEY = "list_mode"
    }
}
