package com.frostfel.animelist.utils

import com.frostfel.animelist.views.AnimeSearchView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun AnimeSearchView.getQueryFlow(): StateFlow<String> {
    val query = MutableStateFlow("")

    submitQueryChange = {
        query.value = it
    }

    return query
}