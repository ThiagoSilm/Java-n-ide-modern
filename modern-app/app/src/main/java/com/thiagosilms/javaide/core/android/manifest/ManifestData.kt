package com.thiagosilms.javaide.core.android.manifest

import java.io.Serializable

/**
 * Classe que contém as informações do AndroidManifest.xml obtidas durante o parsing.
 */
data class ManifestData(
    var packageName: String = "",
    var versionCode: Int? = null,
    var versionName: String? = null,
    var minSdkVersion: Int = 1,
    var targetSdkVersion: Int = 0,
    var debuggable: Boolean = false,
    var launcherActivity: ActivityData? = null,
    val activities: MutableList<ActivityData> = mutableListOf(),
    val services: MutableList<ComponentData> = mutableListOf(),
    val receivers: MutableList<ComponentData> = mutableListOf(),
    val providers: MutableList<ComponentData> = mutableListOf(),
    val usesPermissions: MutableList<String> = mutableListOf(),
    val usesFeatures: MutableList<FeatureData> = mutableListOf(),
    val usesLibraries: MutableList<LibraryData> = mutableListOf(),
    val processes: MutableSet<String> = mutableSetOf()
) {
    data class ActivityData(
        var name: String = "",
        var isLauncher: Boolean = false,
        var isExported: Boolean = false,
        var process: String? = null,
        var theme: String? = null
    ) : Serializable

    data class ComponentData(
        var name: String = "",
        var process: String? = null,
        var exported: Boolean = false
    ) : Serializable

    data class FeatureData(
        var name: String? = null,
        var required: Boolean = true,
        var glEsVersion: Int = 0
    ) : Serializable

    data class LibraryData(
        var name: String = "",
        var required: Boolean = true
    ) : Serializable

    fun addActivity(activity: ActivityData) {
        activities.add(activity)
        if (activity.isLauncher) {
            launcherActivity = activity
        }
    }

    fun addProcess(process: String) {
        processes.add(process)
    }

    fun isValid(): Boolean {
        return packageName.isNotEmpty()
    }
}