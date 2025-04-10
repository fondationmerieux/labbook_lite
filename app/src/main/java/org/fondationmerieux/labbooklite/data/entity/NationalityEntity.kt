package org.fondationmerieux.labbooklite.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "nationality")
data class NationalityEntity(
    @PrimaryKey val nat_ser: Int,
    val nat_name: String,
    val nat_code: String
)
