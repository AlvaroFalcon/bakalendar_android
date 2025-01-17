package com.frostfel.animelist.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Anime (
    @PrimaryKey(autoGenerate = false)
    @SerializedName
    ("mal_id") val malId: Int,
    @SerializedName("url") val url: String?,
    @SerializedName("images") val images: Images,
    @SerializedName("trailer") val trailer: TrailerInfo,
    @SerializedName("title") val title: String?,
    @SerializedName("title_english") val titleEnglish: String?,
    @SerializedName("title_japanese") val titleJapanese: String?,
    @SerializedName("title_synonyms") val titleSynonyms: List<String>,
    @SerializedName("type") val type: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("episodes") val episodes: Int,
    @SerializedName("status") val status: String?,
    @SerializedName("airing") val airing: Boolean,
    @SerializedName("aired") val aired: Aired,
    @SerializedName("duration") val duration: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("score") val score: Double,
    @SerializedName("scored_by") val scoredBy: Int,
    @SerializedName("rank") val rank: Int,
    @SerializedName("popularity") val popularity: Int,
    @SerializedName("members") val members: Int,
    @SerializedName("favorites") val favorites: Int,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("background") val background: String?,
    @SerializedName("season") val season: String?,
    @SerializedName("year") val year: String?,
    @SerializedName("broadcast") val broadcast: Broadcast,
    @SerializedName("producers") val producers: List<GenericMalData>,
    @SerializedName("licensors") val licensors: List<GenericMalData>,
    @SerializedName("studios") val studios: List<GenericMalData>,
    @SerializedName("genres") val genres: List<GenericMalData>,
    @SerializedName("explicit_genres") val explicitGenres: List<GenericMalData>,
    @SerializedName("themes") val themes: List<GenericMalData>,
    @SerializedName("demographics") val demographics: List<GenericMalData>,
    @Deprecated("use from AnimeWithPreferences")
    val starred: Boolean = false,
    var page: Int = 0
) : Parcelable