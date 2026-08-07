package com.example.ade.data

import android.content.Context
import androidx.room.*
import com.example.ade.model.BillRecord
import com.example.ade.model.UsageType

@Dao
interface BillDao {
    @Query("SELECT * FROM billing_history ORDER BY dateMillis DESC")
    suspend fun getAllBills(): List<BillRecord>

    @Insert
    suspend fun insertBill(bill: BillRecord)

    @Delete
    suspend fun deleteBill(bill: BillRecord)
}

class Converters {
    @TypeConverter
    fun fromUsageType(value: UsageType) = value.name

    @TypeConverter
    fun toUsageType(value: String) = UsageType.valueOf(value)
}

@Database(entities = [BillRecord::class], version = 3) // Incrémenté à 3 pour forcer la remise à zéro
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ade_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
