package org.fondationmerieux.labbooklite.database.dao

/**
 * Created by AlC on 11/04/2025.
 */
import androidx.room.*
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity

@Dao
interface PrescriberDao {

    @Query("SELECT * FROM prescriber")
    suspend fun getAll(): List<PrescriberEntity>

    @Query("SELECT * FROM prescriber WHERE id_data = :id")
    suspend fun getById(id: Int): PrescriberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PrescriberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PrescriberEntity)

    @Query("DELETE FROM prescriber")
    suspend fun deleteAll()
}
