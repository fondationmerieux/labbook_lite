package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.AnalysisRequestEntity

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

    @Query("SELECT MAX(id) FROM analysis_request")
    suspend fun getMaxId(): Int?

    @Query("SELECT * FROM analysis_request WHERE recordId = :recordId")
    suspend fun getByRecord(recordId: Int): List<AnalysisRequestEntity>
}
