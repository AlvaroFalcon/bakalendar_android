package com.frostfel.animelist.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RelationsTest {
    private fun entry(id: Int, type: String = "anime", name: String = "Entry $id") =
        RelatedEntry(id, type, name, "https://myanimelist.net/$type/$id", "TV", null)

    @Test
    fun animeFirstByClosenessThenManga() {
        val response = RelationsResponse(
            listOf(
                RelationGroup("Adaptation", listOf(entry(1, type = "manga"))),
                RelationGroup("Side Story", listOf(entry(2))),
                RelationGroup("Sequel", listOf(entry(3))),
                RelationGroup("Something new", listOf(entry(4))),
                RelationGroup("Prequel", listOf(entry(5))),
            )
        )
        val items = response.toRelatedItems()
        assertEquals(listOf(5, 3, 2, 4, 1), items.map { it.malId })
        assertEquals(listOf(true, true, true, true, false), items.map { it.isAnime })
        assertEquals("Prequel", items.first().relation)
    }

    @Test
    fun emptyOrMissingData() {
        assertEquals(emptyList<RelatedItem>(), RelationsResponse(null).toRelatedItems())
        assertEquals(emptyList<RelatedItem>(), RelationsResponse(listOf(RelationGroup("Sequel", null))).toRelatedItems())
    }
}
