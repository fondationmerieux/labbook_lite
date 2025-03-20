package org.fondationmerieux.labbooklite.database

/**
 * Created by AlC on 19/03/2025.
 */
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a patient entity in the database.
 */
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Auto-increment primary key
    val name: String,
    val birthdate: String, // Format: YYYY-MM-DD
    val diagnosis: String
)
