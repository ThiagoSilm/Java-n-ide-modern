package com.thiagosilms.javaide.core.compiler.model

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("compiler_settings")

@Singleton
class CompilerSettings @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Configurações do compilador
    val javaVersion: Flow<String> = dataStore.data.map { prefs ->
        prefs[JAVA_VERSION] ?: "17"
    }

    val useIncrementalCompilation: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[INCREMENTAL_COMPILATION] ?: true
    }

    val useCloudCompilation: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[CLOUD_COMPILATION] ?: false
    }

    val additionalClasspath: Flow<String> = dataStore.data.map { prefs ->
        prefs[ADDITIONAL_CLASSPATH] ?: ""
    }

    suspend fun setJavaVersion(version: String) {
        dataStore.edit { prefs ->
            prefs[JAVA_VERSION] = version
        }
    }

    suspend fun setIncrementalCompilation(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[INCREMENTAL_COMPILATION] = enabled
        }
    }

    suspend fun setCloudCompilation(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[CLOUD_COMPILATION] = enabled
        }
    }

    suspend fun setAdditionalClasspath(classpath: String) {
        dataStore.edit { prefs ->
            prefs[ADDITIONAL_CLASSPATH] = classpath
        }
    }

    companion object {
        private val JAVA_VERSION = stringPreferencesKey("java_version")
        private val INCREMENTAL_COMPILATION = booleanPreferencesKey("incremental_compilation")
        private val CLOUD_COMPILATION = booleanPreferencesKey("cloud_compilation")
        private val ADDITIONAL_CLASSPATH = stringPreferencesKey("additional_classpath")
    }
}