package com.frostfel.animelist.data.typeconverters

import androidx.room.TypeConverter
import com.frostfel.animelist.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


public class DatabaseTypeConverters {
    @TypeConverter
    fun fromGenericMalDataList(genericMalData: List<GenericMalData?>?): String? {
        if (genericMalData == null) {
            return null
        }
        val gson = Gson()
        val type: Type =
            object : TypeToken<List<GenericMalData?>?>() {}.type
        return gson.toJson(genericMalData, type)
    }

    @TypeConverter
    fun toGenericMalData(genericMalDataString: String?): List<GenericMalData>? {
        if (genericMalDataString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<GenericMalData?>?>() {}.type
        return gson.fromJson<List<GenericMalData>>(genericMalDataString, type)
    }

    @TypeConverter
    fun fromTitleSynonyms(titleSynonyms: List<String?>?): String? {
        if (titleSynonyms == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<String?>?>() {}.type
        return gson.toJson(titleSynonyms, type)
    }

    @TypeConverter
    fun toTitleSynonyms(titleSynonymsString: String?): List<String>? {
        if (titleSynonymsString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<String?>?>() {}.type
        return gson.fromJson<List<String>>(titleSynonymsString, type)
    }

    @TypeConverter
    fun fromImages(image: Images): String = Gson().toJson(image)

    @TypeConverter
    fun toImages(imagesString: String): Images = Gson().fromJson(imagesString, Images::class.java)

    @TypeConverter
    fun fromTrailerInfo(trailerInfo: TrailerInfo): String = Gson().toJson(trailerInfo)

    @TypeConverter
    fun toTrailerInfo(trailerInfoString: String): TrailerInfo = Gson().fromJson(trailerInfoString, TrailerInfo::class.java)

    @TypeConverter
    fun fromAired(aired: Aired): String = Gson().toJson(aired)

    @TypeConverter
    fun toAired(airedString: String): Aired = Gson().fromJson(airedString, Aired::class.java)

    @TypeConverter
    fun fromBroadcast(broadcast: Broadcast): String = Gson().toJson(broadcast)

    @TypeConverter
    fun toBroadcast(broadcastString: String): Broadcast = Gson().fromJson(broadcastString, Broadcast::class.java)
}