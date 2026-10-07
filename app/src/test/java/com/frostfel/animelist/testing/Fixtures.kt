package com.frostfel.animelist.testing

import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.SeasonResponse
import com.google.gson.Gson

/** Real Tenrai `seasons/now` entries (see tenrai_seasons_now.json). */
object Fixtures {
    val seasonResponse: SeasonResponse by lazy {
        javaClass.classLoader!!.getResourceAsStream("tenrai_seasons_now.json").reader()
            .use { Gson().fromJson(it, SeasonResponse::class.java) }
    }

    /** "Kusuriya no Hitorigoto 3rd Season": Fridays 23:00 JST, currently airing. */
    val airing: Anime get() = seasonResponse.data[0]

    fun anime(malId: Int, title: String = "Anime $malId"): Anime =
        airing.copy(malId = malId, title = title)
}
