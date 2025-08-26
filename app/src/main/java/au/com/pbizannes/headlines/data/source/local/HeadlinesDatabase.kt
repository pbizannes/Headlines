package au.com.pbizannes.headlines.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import au.com.pbizannes.headlines.domain.model.Article

@Database(
    entities = [Article::class], // List all your entities here
    version = 1,                 // Increment version on schema changes
    exportSchema = false         // Set to true if you want to export schema for migrations
)
public abstract class HeadlinesDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: HeadlinesDatabase? = null

        fun getDatabase(context: Context): HeadlinesDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeadlinesDatabase::class.java,
                    "headlines_database" // Name of your database file
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // .fallbackToDestructiveMigration() // Use with caution during development
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
