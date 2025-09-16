package com.thiagosilms.javaide.core.android.manifest

/**
 * Constantes para o AndroidManifest.xml
 */
object ManifestConstants {
    // Nós do manifest
    const val NODE_MANIFEST = "manifest"
    const val NODE_APPLICATION = "application"
    const val NODE_ACTIVITY = "activity"
    const val NODE_SERVICE = "service"
    const val NODE_RECEIVER = "receiver"
    const val NODE_PROVIDER = "provider"
    const val NODE_USES_SDK = "uses-sdk"
    const val NODE_USES_PERMISSION = "uses-permission"
    const val NODE_USES_FEATURE = "uses-feature"
    const val NODE_USES_LIBRARY = "uses-library"
    const val NODE_INTENT_FILTER = "intent-filter"
    const val NODE_ACTION = "action"
    const val NODE_CATEGORY = "category"

    // Atributos comuns
    const val ATTR_PACKAGE = "package"
    const val ATTR_NAME = "name"
    const val ATTR_PROCESS = "process"
    const val ATTR_EXPORTED = "exported"
    const val ATTR_REQUIRED = "required"
    const val ATTR_VERSION_CODE = "versionCode"
    const val ATTR_VERSION_NAME = "versionName"
    const val ATTR_MIN_SDK = "minSdkVersion"
    const val ATTR_TARGET_SDK = "targetSdkVersion"
    const val ATTR_DEBUGGABLE = "debuggable"
    const val ATTR_THEME = "theme"
    const val ATTR_GLES_VERSION = "glEsVersion"

    // Valores de intent-filter
    const val ACTION_MAIN = "android.intent.action.MAIN"
    const val CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"

    // Namespace Android
    const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    const val ANDROID_NS_PREFIX = "android"
}