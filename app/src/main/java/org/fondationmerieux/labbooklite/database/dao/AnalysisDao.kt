package org.fondationmerieux.labbooklite.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fondationmerieux.labbooklite.database.entity.AnalysisEntity
import org.fondationmerieux.labbooklite.database.model.AnalysisWithFamily

/**
 * Created by AlC on 31/03/2025.
 */
@Dao
interface AnalysisDao {

    @Query("SELECT * FROM analysis")
    suspend fun getAll(): List<AnalysisEntity>

    @Query("SELECT * FROM analysis WHERE id_data = :id")
    suspend fun getById(id: Int): AnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(analysisList: List<AnalysisEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analysis: AnalysisEntity)

    @Query("DELETE FROM analysis")
    suspend fun deleteAll()

    @Query("""
    SELECT 
        a.id_data AS id,
        a.code AS code,
        a.ana_loinc AS ana_loinc,
        a.name AS name,
        d.label AS familyName,
        a.sample_type AS sample_type,
        a.bio_product AS bio_product
    FROM analysis a
    LEFT JOIN dictionary d ON d.id_data = a.family
    WHERE a.active = 4
""")
    suspend fun getAllWithFamily(): List<AnalysisWithFamily>

    @Query("SELECT COUNT(*) FROM analysis WHERE code NOT LIKE 'PB%'")
    fun countWithoutPB(): Int
}
