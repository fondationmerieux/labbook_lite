package org.fondationmerieux.labbooklite.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by AlC on 31/03/2025.
 */
@Entity(tableName = "patient")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id_data: Int,
    val pat_ano: Int?,
    val pat_code_lab: String?,
    val pat_code: String?,
    val pat_name: String?,
    val pat_midname: String?,
    val pat_maiden: String?,
    val pat_firstname: String?,
    val pat_sex: Int?,
    val pat_birth: String?,
    val pat_birth_approx: Int?,
    val pat_age: Int?,
    val pat_age_unit: Int?,
    val pat_nationality: Int?,
    val pat_resident: String?,
    val pat_blood_group: Int?,
    val pat_blood_rhesus: Int?,
    val pat_address: String?,
    val pat_phone1: String?,
    val pat_phone2: String?,
    val pat_profession: String?,
    val pat_zipcode: String?,
    val pat_city: String?,
    val pat_pbox: String?,
    val pat_district: String?,
    val pat_email: String?,
    val pat_lite: Int
)
