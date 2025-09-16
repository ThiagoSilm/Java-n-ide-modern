package com.thiagosilms.javaide.core.android.manifest

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.FileReader
import javax.xml.parsers.SAXParserFactory

/**
 * Parser do AndroidManifest.xml usando SAX para eficiência.
 */
class AndroidManifestParser {

    fun parse(manifestFile: File): ManifestData? {
        return try {
            val handler = ManifestHandler()
            val factory = SAXParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newSAXParser()
            parser.parse(InputSource(FileReader(manifestFile)), handler)
            handler.manifestData.takeIf { it.isValid() }
        } catch (e: Exception) {
            null
        }
    }

    private class ManifestHandler : DefaultHandler() {
        val manifestData = ManifestData()
        private var currentActivity: ManifestData.ActivityData? = null
        private var inApplication = false
        private var inIntentFilter = false
        private var hasMainAction = false
        private var hasLauncherCategory = false

        private var currentLevel = 0
        private var validLevel = 0

        @Throws(SAXException::class)
        override fun startElement(
            uri: String,
            localName: String,
            qName: String,
            attributes: Attributes
        ) {
            currentLevel++
            
            when {
                currentLevel == 1 && localName == ManifestConstants.NODE_MANIFEST -> {
                    parseManifestNode(attributes)
                    validLevel++
                }
                
                currentLevel == 2 -> when (localName) {
                    ManifestConstants.NODE_APPLICATION -> {
                        parseApplicationNode(attributes)
                        inApplication = true
                        validLevel++
                    }
                    ManifestConstants.NODE_USES_SDK -> {
                        parseUsesSdkNode(attributes)
                    }
                }
                
                currentLevel == 3 && inApplication -> when (localName) {
                    ManifestConstants.NODE_ACTIVITY -> {
                        currentActivity = parseActivityNode(attributes)
                        validLevel++
                    }
                    ManifestConstants.NODE_SERVICE -> {
                        manifestData.services.add(parseComponentNode(attributes))
                    }
                    ManifestConstants.NODE_RECEIVER -> {
                        manifestData.receivers.add(parseComponentNode(attributes))
                    }
                    ManifestConstants.NODE_PROVIDER -> {
                        manifestData.providers.add(parseComponentNode(attributes))
                    }
                }
                
                currentLevel == 4 && currentActivity != null -> {
                    if (localName == ManifestConstants.NODE_INTENT_FILTER) {
                        inIntentFilter = true
                        hasMainAction = false
                        hasLauncherCategory = false
                        validLevel++
                    }
                }
                
                currentLevel == 5 && inIntentFilter -> when (localName) {
                    ManifestConstants.NODE_ACTION -> {
                        val action = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_NAME)
                        if (action == ManifestConstants.ACTION_MAIN) {
                            hasMainAction = true
                        }
                    }
                    ManifestConstants.NODE_CATEGORY -> {
                        val category = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_NAME)
                        if (category == ManifestConstants.CATEGORY_LAUNCHER) {
                            hasLauncherCategory = true
                        }
                    }
                }
            }
        }

        @Throws(SAXException::class)
        override fun endElement(uri: String, localName: String, qName: String) {
            if (validLevel == currentLevel) {
                when (localName) {
                    ManifestConstants.NODE_APPLICATION -> {
                        inApplication = false
                    }
                    ManifestConstants.NODE_ACTIVITY -> {
                        currentActivity?.let { activity ->
                            manifestData.addActivity(activity)
                        }
                        currentActivity = null
                    }
                    ManifestConstants.NODE_INTENT_FILTER -> {
                        if (hasMainAction && hasLauncherCategory) {
                            currentActivity?.isLauncher = true
                        }
                        inIntentFilter = false
                    }
                }
                validLevel--
            }
            currentLevel--
        }

        private fun parseManifestNode(attributes: Attributes) {
            manifestData.packageName = attributes.getValue(ManifestConstants.ATTR_PACKAGE) ?: ""
            manifestData.versionCode = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_VERSION_CODE)?.toIntOrNull()
            manifestData.versionName = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_VERSION_NAME)
        }

        private fun parseApplicationNode(attributes: Attributes) {
            manifestData.debuggable = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_DEBUGGABLE)?.toBoolean() ?: false
            attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_PROCESS)?.let { process ->
                manifestData.addProcess(process)
            }
        }

        private fun parseUsesSdkNode(attributes: Attributes) {
            manifestData.minSdkVersion = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_MIN_SDK)?.toIntOrNull() ?: 1
            manifestData.targetSdkVersion = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_TARGET_SDK)?.toIntOrNull() ?: 0
        }

        private fun parseActivityNode(attributes: Attributes): ManifestData.ActivityData {
            val name = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_NAME) ?: ""
            return ManifestData.ActivityData(
                name = name,
                isExported = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_EXPORTED)?.toBoolean() ?: false,
                process = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_PROCESS),
                theme = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_THEME)
            )
        }

        private fun parseComponentNode(attributes: Attributes): ManifestData.ComponentData {
            return ManifestData.ComponentData(
                name = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_NAME) ?: "",
                process = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_PROCESS),
                exported = attributes.getValue(ManifestConstants.ANDROID_NS, ManifestConstants.ATTR_EXPORTED)?.toBoolean() ?: false
            )
        }
    }
}