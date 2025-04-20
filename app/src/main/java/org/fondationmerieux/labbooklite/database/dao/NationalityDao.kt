package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.NationalityEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface NationalityDao {

    @Query("SELECT * FROM nationality")
    suspend fun getAll(): List<NationalityEntity>

    @Query("SELECT * FROM nationality WHERE nat_ser = :id")
    suspend fun getById(id: Int): NationalityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<NationalityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NationalityEntity)

    @Query("DELETE FROM nationality")
    suspend fun deleteAll()
}