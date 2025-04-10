package org.fondationmerieux.labbooklite.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.data.entity.AnalysisValidationEntity

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
    suspend fun insertAll(validations: List<AnalysisValidationEntity>)
}
