package org.fondationmerieux.labbooklite.converter

/**
 * Created by AlC on 31/03/2025.
 */
import androidx.room.TypeConverter
import java.util.Date

object Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}