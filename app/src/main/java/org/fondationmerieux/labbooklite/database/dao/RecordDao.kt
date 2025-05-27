package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import org.fondationmerieux.labbooklite.database.model.RecordWithPatient


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
    suspend fun insert(entry: RecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<RecordEntity>): List<Long>

    @Query("DELETE FROM record")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM record")
    suspend fun getAllWithPatient(): List<RecordWithPatient>

    @Query("SELECT MAX(id_data) FROM record")
    suspend fun getMaxRecordId(): Int?

    @Query("SELECT rec_num_lite FROM record WHERE rec_num_lite LIKE 'LT-%' ORDER BY rec_num_lite DESC LIMIT 1")
    suspend fun getLastLiteRecordNumber(): String?

    @Query("SELECT * FROM record ORDER BY id_data DESC LIMIT 1")
    fun getLastRecord(): RecordEntity?

    @Query("SELECT COUNT(*) FROM record")
    fun count(): Int

    @Update
    suspend fun update(record: RecordEntity)

    @Delete
    fun delete(record: RecordEntity)
}
