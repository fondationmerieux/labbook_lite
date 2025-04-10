package org.fondationmerieux.labbooklite.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.data.entity.RecordEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface RecordDao {

    @Query("SELECT * FROM record")
    suspend fun getAll(): List<RecordEntity>

    @Query("SELECT * FROM record WHERE id_data = :id")
    suspend fun getById(id: Int): RecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<RecordEntity>)

    @Query("DELETE FROM record")
    suspend fun deleteAll()
}
