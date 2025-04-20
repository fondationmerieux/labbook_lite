package org.fondationmerieux.labbooklite.adapter

/**
 * Created by AlC on 31/03/2025.
 */

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateJsonAdapter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    @ToJson
    fun toJson(date: Date?): String? = date?.let { dateFormat.format(it) }

    @FromJson
    fun fromJson(dateString: String?): Date? = dateString?.let { dateFormat.parse(it) }
}
