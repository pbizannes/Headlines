package au.com.pbizannes.headlines.data.preferences

import androidx.datastore.preferences.core.stringSetPreferencesKey

object UserPreferencesKeys {
    // selected news source IDs preference (e.g., "abc-news", "cnn")
    val SELECTED_SOURCE_IDS = stringSetPreferencesKey("selected_source_ids")
}
