package au.com.pbizannes.headlines.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import au.com.pbizannes.headlines.data.models.ArticleData
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(articleData: ArticleData)

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<ArticleData>>

    @Query("SELECT * FROM articles WHERE url = :articleUrl")
    suspend fun getArticleByUrl(articleUrl: String): ArticleData?

    @Delete
    suspend fun deleteArticle(articleData: ArticleData)

    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE url = :url LIMIT 1)")
    fun isArticleBookmarked(url: String): Flow<Boolean>
}