package au.com.pbizannes.headlines.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import au.com.pbizannes.headlines.domain.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Replace if article with same URL already exists
    suspend fun insertArticle(article: Article)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllArticles(articles: List<Article>)

    @Query("SELECT * FROM articles ORDER BY publishedAt DESC") // Get all articles, newest first
    fun getAllArticles(): Flow<List<Article>> // Use Flow for reactive updates

    @Query("SELECT * FROM articles WHERE url = :articleUrl")
    suspend fun getArticleByUrl(articleUrl: String): Article?

    @Query("SELECT * FROM articles WHERE title LIKE :query OR description LIKE :query ORDER BY publishedAt DESC")
    fun searchArticles(query: String): Flow<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE url = :url LIMIT 1)")
    fun isArticleBookmarked(url: String): Flow<Boolean>
}