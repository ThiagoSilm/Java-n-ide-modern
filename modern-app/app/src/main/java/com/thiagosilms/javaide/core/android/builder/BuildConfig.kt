package com.thiagosilms.javaide.core.android.builder

import java.io.File

/**
 * Configurações para o build de APK
 */
data class BuildConfig(
    val applicationId: String,
    val versionCode: Int,
    val versionName: String,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val debuggable: Boolean = true,
    val debugKeystore: File? = null,
    val releaseKeystore: File? = null,
    val keystorePassword: String = "",
    val keyAlias: String = "",
    val keyPassword: String = "",
    val aaptPath: String = "aapt",
    val androidJar: File? = null,
    val dxPath: String = "dx"
) {
    class Builder {
        private var applicationId: String = ""
        private var versionCode: Int = 1
        private var versionName: String = "1.0"
        private var minSdkVersion: Int = 21
        private var targetSdkVersion: Int = 33
        private var debuggable: Boolean = true
        private var debugKeystore: File? = null
        private var releaseKeystore: File? = null
        private var keystorePassword: String = ""
        private var keyAlias: String = ""
        private var keyPassword: String = ""
        private var aaptPath: String = "aapt"
        private var androidJar: File? = null
        private var dxPath: String = "dx"

        fun setApplicationId(id: String) = apply { this.applicationId = id }
        fun setVersionCode(code: Int) = apply { this.versionCode = code }
        fun setVersionName(name: String) = apply { this.versionName = name }
        fun setMinSdkVersion(version: Int) = apply { this.minSdkVersion = version }
        fun setTargetSdkVersion(version: Int) = apply { this.targetSdkVersion = version }
        fun setDebuggable(debug: Boolean) = apply { this.debuggable = debug }
        fun setDebugKeystore(keystore: File) = apply { this.debugKeystore = keystore }
        fun setReleaseKeystore(keystore: File) = apply { this.releaseKeystore = keystore }
        fun setKeystorePassword(password: String) = apply { this.keystorePassword = password }
        fun setKeyAlias(alias: String) = apply { this.keyAlias = alias }
        fun setKeyPassword(password: String) = apply { this.keyPassword = password }
        fun setAaptPath(path: String) = apply { this.aaptPath = path }
        fun setAndroidJar(jar: File) = apply { this.androidJar = jar }
        fun setDxPath(path: String) = apply { this.dxPath = path }

        fun build(): BuildConfig {
            require(applicationId.isNotEmpty()) { "ApplicationId é obrigatório" }
            require(androidJar != null) { "Android.jar é obrigatório" }
            
            return BuildConfig(
                applicationId = applicationId,
                versionCode = versionCode,
                versionName = versionName,
                minSdkVersion = minSdkVersion,
                targetSdkVersion = targetSdkVersion,
                debuggable = debuggable,
                debugKeystore = debugKeystore,
                releaseKeystore = releaseKeystore,
                keystorePassword = keystorePassword,
                keyAlias = keyAlias,
                keyPassword = keyPassword,
                aaptPath = aaptPath,
                androidJar = androidJar,
                dxPath = dxPath
            )
        }
    }
}