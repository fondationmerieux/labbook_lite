package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.AnalysisValidationEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface AnalysisValidationDao {

    @Query("SELECT * FROM analysis_validation")
    suspend fun getAll(): List<AnalysisValidationEntity>

    @Query("SELECT * FROM analysis_validation WHERE id = :id")
    suspend fun getById(id: Int): AnalysisValidationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(validations: List<AnalysisValidationEntity>): List<Long>

    @Query("DELETE FROM analysis_validation")
    suspend fun deleteAll()

    @Query("SELECT MAX(id) FROM analysis_validation")
    suspend fun getMaxId(): Int?

    @Query("DELETE FROM analysis_validation")
    suspend fun clearValidations()

    @Query("DELETE FROM analysis_validation WHERE resultId = :resultId")
    fun deleteByResultId(resultId: Int)
}
