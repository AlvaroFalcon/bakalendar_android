package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

/** `GET anime/{id}/recommendations`: user recommendations, most voted first. */
data class RecommendationsResponse(
    @SerializedName("data") val data: List<Recommendation>?
)

data class Recommendation(
    @SerializedName("entry") val entry: RecommendationEntry?,
    @SerializedName("votes") val votes: Int?
)

data class RecommendationEntry(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("images") val images: Images?
)

fun RecommendationsResponse.toRelatedItems(limit: Int = 15): List<RelatedItem> =
    data.orEmpty()
        .mapNotNull { recommendation -> recommendation.entry?.let { it to (recommendation.votes ?: 0) } }
        .sortedByDescending { it.second }
        .take(limit)
        .map { (entry, _) ->
            RelatedItem(
                malId = entry.malId,
                name = entry.title.orEmpty(),
                relation = "",
                mediaType = null,
                imageUrl = entry.images?.webp?.largeImageUrl ?: entry.images?.jpg?.largeImageUrl,
                url = entry.url,
                isAnime = true,
            )
        }
