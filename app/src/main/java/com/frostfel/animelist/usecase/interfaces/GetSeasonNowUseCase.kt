package com.frostfel.animelist.usecase.interfaces

import android.content.Context
import com.frostfel.animelist.model.Anime

interface GetSeasonNowUseCase {
    suspend fun invoke(context: Context): List<Anime>
}