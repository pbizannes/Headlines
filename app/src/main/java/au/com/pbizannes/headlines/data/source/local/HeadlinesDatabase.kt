package au.com.pbizannes.headlines.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import au.com.pbizannes.headlines.data.models.ArticleData

@Database(
    entities = [ArticleData::class],
    version = 1,
    exportSchema = false
)
abstract class HeadlinesDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: HeadlinesDatabase? = null

        fun getDatabase(context: Context): HeadlinesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeadlinesDatabase::class.java,
                    "headlines_database"
                )
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}
