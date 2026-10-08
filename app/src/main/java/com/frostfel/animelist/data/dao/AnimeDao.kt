package com.frostfel.animelist.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimePreferences
import com.frostfel.animelist.model.AnimeWithPreferences
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animes: List<Anime>)

    /** Current season, in the order it was downloaded (new shows first). */
    @Transaction
    @Query("SELECT * FROM anime WHERE page > 0 ORDER BY page")
    fun seasonWithPreferencesFlow(): Flow<List<AnimeWithPreferences>>

    @Query("SELECT * FROM anime WHERE page > 0")
    fun seasonFlow(): Flow<List<Anime>>

    /** Current season plus favourites from previous seasons. */
    @Transaction
    @Query(
        """
        SELECT * FROM anime
        WHERE page > 0
        OR malId IN (SELECT malId FROM user_anime_preferences WHERE starred = 1)
        """
    )
    fun calendarFlow(): Flow<List<AnimeWithPreferences>>

    @Query("SELECT COUNT(*) FROM anime WHERE page > 0")
    suspend fun countSeason(): Int

    /**
     * Drops what is no longer in the season. Favourites are kept (with page = 0) so they
     * survive a season change and stay available offline.
     */
    @Query(
        """
        DELETE FROM anime
        WHERE malId NOT IN (:seasonIds)
        AND malId NOT IN (SELECT malId FROM user_anime_preferences WHERE starred = 1)
        """
    )
    suspend fun deleteOutsideSeasonExceptFavourites(seasonIds: List<Int>)

    @Query("UPDATE anime SET page = 0 WHERE malId NOT IN (:seasonIds)")
    suspend fun markOutsideSeason(seasonIds: List<Int>)

    @Query(
        """
        SELECT * FROM anime
        WHERE page = 0 AND airing = 1
        AND malId IN (SELECT malId FROM user_anime_preferences WHERE starred = 1)
        """
    )
    suspend fun getAiringFavouritesOutsideSeason(): List<Anime>

    @Transaction
    @Query("SELECT * FROM anime WHERE malId = :id")
    fun findAnimeWithPreferencesByIdLiveData(id: Int): LiveData<AnimeWithPreferences?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setStarred(animePreferences: AnimePreferences)

    @Query("DELETE FROM user_anime_preferences WHERE malId = :malId")
    suspend fun removeStarred(malId: Int)

    @Transaction
    @Query(
        """
        SELECT anime.* FROM anime
        INNER JOIN user_anime_preferences AS prefs ON prefs.malId = anime.malId
        WHERE prefs.starred = 1
        ORDER BY prefs.starredAt DESC
        """
    )
    suspend fun getAllFav(): List<AnimeWithPreferences>

    @Transaction
    @Query(
        """
        SELECT anime.* FROM anime
        INNER JOIN user_anime_preferences AS prefs ON prefs.malId = anime.malId
        WHERE prefs.starred = 1
        ORDER BY prefs.starredAt DESC
        """
    )
    fun getAllFavFlow(): Flow<List<AnimeWithPreferences>>
}
