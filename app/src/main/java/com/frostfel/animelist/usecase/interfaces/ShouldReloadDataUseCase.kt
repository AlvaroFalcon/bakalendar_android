package com.frostfel.animelist.usecase.interfaces

import android.content.Context

interface ShouldReloadDataUseCase {
    fun invoke(context: Context): Boolean
}