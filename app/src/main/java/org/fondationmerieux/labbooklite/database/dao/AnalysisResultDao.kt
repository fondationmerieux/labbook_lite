package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.AnalysisResultEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface AnalysisResultDao {

    @Query("SELECT * FROM analysis_result")
    suspend fun getAll(): List<AnalysisResultEntity>

    @Query("SELECT * FROM analysis_result WHERE id = :id")
    suspend fun getById(id: Int): AnalysisResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<AnalysisResultEntity>)

    @Query("SELECT MAX(id) FROM analysis_result")
    suspend fun getMaxId(): Int?
}
