package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.AnaVarEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface AnaVarDao {

    @Query("SELECT * FROM ana_var")
    suspend fun getAll(): List<AnaVarEntity>

    @Query("SELECT * FROM ana_var WHERE id_data = :id")
    suspend fun getById(id: Int): AnaVarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(varList: List<AnaVarEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(variable: AnaVarEntity)

    @Query("DELETE FROM ana_var")
    suspend fun deleteAll()
}
