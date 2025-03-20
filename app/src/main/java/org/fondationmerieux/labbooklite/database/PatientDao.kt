package org.fondationmerieux.labbooklite.database

import androidx.room.*

/**
 * Created by AlC on 19/03/2025.
 */
/**
 * DAO (Data Access Object) for managing Patient entity operations.
 */
@Dao
interface PatientDao {

    /**
     * Inserts a new patient into the database.
     * If a conflict occurs, replaces the existing entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    /**
     * Retrieves all patients from the database.
     */
    @Query("SELECT * FROM patients")
    suspend fun getAllPatients(): List<Patient>

    /**
     * Deletes a specific patient from the database.
     */
    @Delete
    suspend fun delete(patient: Patient)
}