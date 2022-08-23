package com.frostfel.animelist.usecase

import android.content.Context
import com.frostfel.animelist.data.storage.DataPreferenceStore
import com.frostfel.animelist.usecase.interfaces.ShouldReloadDataUseCase
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ShouldReloadDataUseCaseImpl : ShouldReloadDataUseCase {
    override fun invoke(context: Context): Boolean {
        val lastUpdated = DataPreferenceStore.getLastUpdate(context) ?: return true
        val now = LocalDateTime.now()
        if (ChronoUnit.HOURS.between(lastUpdated, now) > 1) {
            return true
        }
        return false
    }
}