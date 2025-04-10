package org.fondationmerieux.labbooklite.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.data.entity.AnaLinkEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface AnaLinkDao {

    @Query("SELECT * FROM ana_link")
    suspend fun getAll(): List<AnaLinkEntity>

    @Query("SELECT * FROM ana_link WHERE id_data = :id")
    suspend fun getById(id: Int): AnaLinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(linkList: List<AnaLinkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: AnaLinkEntity)

    @Query("DELETE FROM ana_link")
    suspend fun deleteAll()
}
