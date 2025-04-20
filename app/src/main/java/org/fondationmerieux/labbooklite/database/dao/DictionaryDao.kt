package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface DictionaryDao {

    @Query("SELECT * FROM dictionary")
    suspend fun getAll(): List<DictionaryEntity>

    @Query("SELECT * FROM dictionary WHERE id_data = :id")
    suspend fun getById(id: Int): DictionaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DictionaryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DictionaryEntity)

    @Query("DELETE FROM dictionary")
    suspend fun deleteAll()

    @Query("SELECT * FROM dictionary WHERE dico_name = :name ORDER BY position")
    suspend fun getByName(name: String): List<DictionaryEntity>
}
