package org.fondationmerieux.labbooklite.database

/**
 * Created by AlC on 19/03/2025.
 */
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Room database with SQLCipher encryption.
 */
@Database(entities = [Patient::class], version = 1, exportSchema = false)
abstract class LabBookLiteDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao

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
                // Convert the password into a byte array for SQLCipher encryption
                val passphrase: ByteArray = SQLiteDatabase.getBytes(password.toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LabBookLiteDatabase::class.java,
                    "labbooklite_encrypted.db"
                ).openHelperFactory(factory) // ✅ Uses SQLCipher for encryption
                    .fallbackToDestructiveMigration() // Deletes and recreates the DB if schema changes
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}