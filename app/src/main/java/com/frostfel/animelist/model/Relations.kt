package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

/** `GET anime/{id}/relations`: related entries grouped by relation (Prequel, Sequel…). */
data class RelationsResponse(
    @SerializedName("data") val data: List<RelationGroup>?
)

data class RelationGroup(
    @SerializedName("relation") val relation: String?,
    @SerializedName("entry") val entry: List<RelatedEntry>?
)

data class RelatedEntry(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("images") val images: Images?
)

/** One card in the "Related" carousel. */
data class RelatedItem(
    val malId: Int,
    val name: String,
    val relation: String,
    val mediaType: String?,
    val imageUrl: String?,
    val url: String?,
    val isAnime: Boolean,
)

private val RELATION_ORDER = listOf(
    "Prequel", "Sequel", "Parent Story", "Full Story", "Side Story", "Summary",
    "Alternative Version", "Alternative Setting", "Spin-Off", "Character", "Other", "Adaptation"
)

/** Flattens the groups: anime first, by how closely related they are; manga last. */
fun RelationsResponse.toRelatedItems(): List<RelatedItem> =
    data.orEmpty().flatMap { group ->
        group.entry.orEmpty().map { entry ->
            RelatedItem(
                malId = entry.malId,
                name = entry.name.orEmpty(),
                relation = group.relation.orEmpty(),
                mediaType = entry.mediaType,
                imageUrl = entry.images?.webp?.largeImageUrl ?: entry.images?.jpg?.largeImageUrl,
                url = entry.url,
                isAnime = entry.type == "anime",
            )
        }
    }.sortedWith(
        compareBy<RelatedItem> { !it.isAnime }
            .thenBy { RELATION_ORDER.indexOf(it.relation).let { index -> if (index < 0) RELATION_ORDER.size else index } }
    )
