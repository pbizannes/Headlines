package au.com.pbizannes.headlines.data.preferences

import androidx.datastore.preferences.core.stringSetPreferencesKey

object UserPreferencesKeys {
    // Key for storing a Set of selected news source IDs (e.g., "abc-news", "cnn")
    val SELECTED_SOURCE_IDS = stringSetPreferencesKey("selected_source_ids")
}
