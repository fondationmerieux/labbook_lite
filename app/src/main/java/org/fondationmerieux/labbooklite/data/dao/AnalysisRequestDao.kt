package org.fondationmerieux.labbooklite.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.data.entity.AnalysisRequestEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface AnalysisRequestDao {

    @Query("SELECT * FROM analysis_request")
    suspend fun getAll(): List<AnalysisRequestEntity>

    @Query("SELECT * FROM analysis_request WHERE id = :id")
    suspend fun getById(id: Int): AnalysisRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AnalysisRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<AnalysisRequestEntity>)

    @Query("DELETE FROM analysis_request")
    suspend fun deleteAll()
}
