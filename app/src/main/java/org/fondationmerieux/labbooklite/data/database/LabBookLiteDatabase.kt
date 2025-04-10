package org.fondationmerieux.labbooklite.database

/**
 * Created by AlC on 31/03/2025.
 */

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.fondationmerieux.labbooklite.data.converter.Converters
import net.sqlcipher.database.SupportFactory
import org.fondationmerieux.labbooklite.data.dao.*
import org.fondationmerieux.labbooklite.data.entity.*

/**
 * Room database with SQLCipher encryption.
 */
@Database(
    entities = [
        UserEntity::class,
        PatientEntity::class,
        PreferencesEntity::class,
        NationalityEntity::class,
        DictionaryEntity::class,
        AnalysisEntity::class,
        AnaLinkEntity::class,
        AnaVarEntity::class,
        SampleEntity::class,
        RecordEntity::class,
        AnalysisRequestEntity::class,
        AnalysisResultEntity::class,
        AnalysisValidationEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LabBookLiteDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun patientDao(): PatientDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun nationalityDao(): NationalityDao
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun analysisDao(): AnalysisDao
    abstract fun anaLinkDao(): AnaLinkDao
    abstract fun anaVarDao(): AnaVarDao
    abstract fun sampleDao(): SampleDao
    abstract fun recordDao(): RecordDao
    abstract fun analysisRequestDao(): AnalysisRequestDao
    abstract fun analysisResultDao(): AnalysisResultDao
    abstract fun analysisValidationDao(): AnalysisValidationDao

    companion object {
        @Volatile
        private var INSTANCE: LabBookLiteDatabase? = null

        /**
         * Returns a singleton instance of the encrypted database.
         * @param context Application context
         * @param password Database encryption password
         */
        fun getDatabase(context: Context, password: String): LabBookLiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase: ByteArray = net.sqlcipher.database.SQLiteDatabase.getBytes(password.toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LabBookLiteDatabase::class.java,
                    "labbooklite_encrypted.db"
                )
                    .openHelperFactory(factory) // 🔒 Uses SQLCipher for encryption
                    .fallbackToDestructiveMigration() // Deletes and recreates DB if schema changes
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
