package org.fondationmerieux.labbooklite.database

/**
 * Created by AlC on 31/03/2025.
 */

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.sqlcipher.database.SQLiteDatabase
import org.fondationmerieux.labbooklite.converter.Converters
import net.sqlcipher.database.SupportFactory
import org.fondationmerieux.labbooklite.database.dao.AnaLinkDao
import org.fondationmerieux.labbooklite.database.dao.AnaVarDao
import org.fondationmerieux.labbooklite.database.dao.AnalysisDao
import org.fondationmerieux.labbooklite.database.dao.AnalysisRequestDao
import org.fondationmerieux.labbooklite.database.dao.AnalysisResultDao
import org.fondationmerieux.labbooklite.database.dao.AnalysisValidationDao
import org.fondationmerieux.labbooklite.database.dao.DictionaryDao
import org.fondationmerieux.labbooklite.database.dao.NationalityDao
import org.fondationmerieux.labbooklite.database.dao.PatientDao
import org.fondationmerieux.labbooklite.database.dao.PreferencesDao
import org.fondationmerieux.labbooklite.database.dao.PrescriberDao
import org.fondationmerieux.labbooklite.database.dao.RecordDao
import org.fondationmerieux.labbooklite.database.dao.SampleDao
import org.fondationmerieux.labbooklite.database.dao.UserDao
import org.fondationmerieux.labbooklite.database.entity.AnaLinkEntity
import org.fondationmerieux.labbooklite.database.entity.AnaVarEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisRequestEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisResultEntity
import org.fondationmerieux.labbooklite.database.entity.AnalysisValidationEntity
import org.fondationmerieux.labbooklite.database.entity.DictionaryEntity
import org.fondationmerieux.labbooklite.database.entity.NationalityEntity
import org.fondationmerieux.labbooklite.database.entity.PatientEntity
import org.fondationmerieux.labbooklite.database.entity.PreferencesEntity
import org.fondationmerieux.labbooklite.database.entity.PrescriberEntity
import org.fondationmerieux.labbooklite.database.entity.RecordEntity
import org.fondationmerieux.labbooklite.database.entity.SampleEntity
import org.fondationmerieux.labbooklite.database.entity.UserEntity

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
        AnalysisValidationEntity::class,
        PrescriberEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LabBookLiteDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun patientDao(): PatientDao
    abstract fun prescriberDao(): PrescriberDao
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
                val dbName = "labbooklite_encrypted.db"
                val dbFile = context.getDatabasePath(dbName)

                // Initialize SQLCipher native libraries
                SQLiteDatabase.loadLibs(context)

                // Convert password to char array for SQLCipher compatibility
                val passphraseChars = password.toCharArray()

                // If the database exists, verify it can be opened with the given password
                if (dbFile.exists()) {
                    try {
                        val testDb = SQLiteDatabase.openDatabase(
                            dbFile.absolutePath,
                            passphraseChars,
                            null,
                            SQLiteDatabase.OPEN_READONLY
                        )
                        testDb.close()
                    } catch (e: Exception) {
                        dbFile.delete()
                    }
                }

                val passphrase = SQLiteDatabase.getBytes(passphraseChars)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LabBookLiteDatabase::class.java,
                    dbName
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        fun recreate(context: Context, password: String) {
            INSTANCE = null
            getDatabase(context, password)
        }
    }
}
