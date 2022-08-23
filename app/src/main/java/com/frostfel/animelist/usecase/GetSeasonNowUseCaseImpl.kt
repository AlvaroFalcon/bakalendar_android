package com.frostfel.animelist.usecase

import android.content.Context
import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.data.storage.DataPreferenceStore
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.usecase.interfaces.GetSeasonNowUseCase
import com.frostfel.animelist.usecase.interfaces.ShouldReloadDataUseCase
import javax.inject.Inject

class GetSeasonNowUseCaseImpl @Inject constructor(
    private val apiServices: ApiServices,
    private val shouldReloadDataUseCase: ShouldReloadDataUseCase
): GetSeasonNowUseCase {
    override suspend fun invoke(context: Context): List<Anime> {
        val shouldReloadData = shouldReloadDataUseCase.invoke(context)
        return if (shouldReloadData) {
            val data = apiServices.getCurrentSeason().data
            DataPreferenceStore.saveData(data, context)
            data
        } else {
            DataPreferenceStore.getData(context)
        }
    }
}