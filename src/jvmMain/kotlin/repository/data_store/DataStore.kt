package repository.data_store

import androidx.compose.runtime.collectAsState
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.File

val dataStore = PreferenceDataStoreFactory.create {
    File("./app_data.preferences_pb")
}

private suspend fun String.saveToDataStore(key: String) {
    dataStore.edit {
        it[stringPreferencesKey(key)] = this
    }
}

fun <T> T.saveToDataStore(key: DSKey) = runBlocking {
    Gson().toJson(this@saveToDataStore).saveToDataStore(key.name)
}

inline fun <reified T> DSKey.getFromDataStore(default: T): T {
    var value = default
    runBlocking {
        dataStore.data.first { preferences ->
            preferences[stringPreferencesKey(name)]?.let {
                value = Gson().fromJson(it, T::class.java)
            }
            true
        }
    }
    return value
}


