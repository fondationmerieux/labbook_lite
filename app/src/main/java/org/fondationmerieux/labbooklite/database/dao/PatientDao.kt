package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.PatientEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface PatientDao {

    @Query("SELECT * FROM patient")
    suspend fun getAll(): List<PatientEntity>

    @Query("SELECT * FROM patient WHERE id_data = :id")
    suspend fun getById(id: Int): PatientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PatientEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PatientEntity): Long

    @Update
    suspend fun update(patient: PatientEntity)

    @Query("DELETE FROM patient")
    suspend fun deleteAll()

    @Query("""
    SELECT * FROM patient
    WHERE pat_name LIKE '%' || :query || '%' 
       OR pat_firstname LIKE '%' || :query || '%'
       OR pat_code LIKE '%' || :query || '%'
       OR pat_code_lab LIKE '%' || :query || '%'
       OR pat_phone1 LIKE '%' || :query || '%'
       OR pat_phone2 LIKE '%' || :query || '%'
       OR pat_email LIKE '%' || :query || '%'
""")
    suspend fun searchPatients(query: String): List<PatientEntity>

    @Query("SELECT * FROM patient WHERE pat_code_lab = :codeLab LIMIT 1")
    fun getByCodeLab(codeLab: String): PatientEntity?

    @Query("SELECT * FROM patient WHERE pat_code = :code LIMIT 1")
    fun getByCode(code: String): PatientEntity?

    @Query("SELECT COUNT(*) FROM patient")
    fun count(): Int
}
