package org.fondationmerieux.labbooklite.database.entity

/**
 * Created by AlC on 11/04/2025.
 */
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriber")
data class PrescriberEntity(
    @PrimaryKey val id_data: Int,
    val code: String?,
    val lastname: String?,
    val firstname: String?,
    val city: String?,
    val institution: String?,
    val speciality: Int?,
    val phone: String?,
    val email: String?,
    val title: Int?,
    val initial: String?,
    val department: String?,
    val address: String?,
    val mobile: String?,
    val fax: String?,
    val zip_city: String?
)
