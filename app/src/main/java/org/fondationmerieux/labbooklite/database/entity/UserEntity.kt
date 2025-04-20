package org.fondationmerieux.labbooklite.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id_data: Int,
    val firstname: String?,
    val lastname: String?,
    val username: String?,
    val password: String?,
    val title: Int?,
    val email: String?,
    val locale: Int?,
    val initial: String?,
    val role_type: String?
)
