package org.fondationmerieux.labbooklite.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.data.entity.SampleEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface SampleDao {

    @Query("SELECT * FROM sample")
    suspend fun getAll(): List<SampleEntity>

    @Query("SELECT * FROM sample WHERE id_data = :id")
    suspend fun getById(id: Int): SampleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SampleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SampleEntity>)

    @Query("DELETE FROM sample")
    suspend fun deleteAll()
}
