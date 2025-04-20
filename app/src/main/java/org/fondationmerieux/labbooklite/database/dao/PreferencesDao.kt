package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.PreferencesEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface PreferencesDao {

    @Query("SELECT * FROM preferences")
    suspend fun getAll(): List<PreferencesEntity>

    @Query("SELECT * FROM preferences WHERE id_data = :id")
    suspend fun getById(id: Int): PreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PreferencesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PreferencesEntity)

    @Query("DELETE FROM preferences")
    suspend fun deleteAll()
}
