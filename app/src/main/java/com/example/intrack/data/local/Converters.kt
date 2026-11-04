package com.example.intrack.data.local

import androidx.room.TypeConverter
import com.example.intrack.data.model.GoalContributionEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromContributionList(value: List<GoalContributionEntity>?): String {
        return gson.toJson(value ?: emptyList<GoalContributionEntity>())
    }

    @TypeConverter
    fun toContributionList(value: String?): List<GoalContributionEntity> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<GoalContributionEntity>>() {}.type
        return try {
            gson.fromJson(value, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
