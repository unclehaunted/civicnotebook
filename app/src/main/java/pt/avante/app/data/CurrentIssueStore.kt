package pt.avante.app.data

import android.content.Context
import android.content.SharedPreferences

class CurrentIssueStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pt.avante.app.current_issue", Context.MODE_PRIVATE)

    var lastKnownCurrentIssueNumber: String?
        get() = prefs.getString(KEY_ISSUE_NUMBER, null)
        set(value) {
            prefs.edit().putString(KEY_ISSUE_NUMBER, value).apply()
        }

    var feedSnapshot: pt.avante.app.data.FeedPresentationSnapshot?
        get() {
            val json = prefs.getString(KEY_FEED_SNAPSHOT, null) ?: return null
            return try {
                kotlinx.serialization.json.Json.decodeFromString(pt.avante.app.data.FeedPresentationSnapshot.serializer(), json)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_FEED_SNAPSHOT).apply()
            } else {
                val json = kotlinx.serialization.json.Json.encodeToString(pt.avante.app.data.FeedPresentationSnapshot.serializer(), value)
                prefs.edit().putString(KEY_FEED_SNAPSHOT, json).apply()
            }
        }

    companion object {
        private const val KEY_ISSUE_NUMBER = "last_known_current_issue_number"
        private const val KEY_FEED_SNAPSHOT = "last_known_feed_snapshot"
    }
}
