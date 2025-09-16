package com.thiagosilms.javaide.core.android.manifest

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AndroidManifestParserTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var parser: AndroidManifestParser
    private lateinit var manifestFile: File

    @Before
    fun setup() {
        parser = AndroidManifestParser()
    }

    @Test
    fun `parse basic manifest`() {
        val manifest = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.example.app"
                android:versionCode="1"
                android:versionName="1.0">
                <uses-sdk
                    android:minSdkVersion="21"
                    android:targetSdkVersion="33" />
                <application
                    android:debuggable="true">
                    <activity
                        android:name=".MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent()

        manifestFile = tempFolder.newFile("AndroidManifest.xml")
        manifestFile.writeText(manifest)

        val result = parser.parse(manifestFile)

        assertNotNull(result)
        assertEquals("com.example.app", result!!.packageName)
        assertEquals(1, result.versionCode)
        assertEquals("1.0", result.versionName)
        assertEquals(21, result.minSdkVersion)
        assertEquals(33, result.targetSdkVersion)
        assertTrue(result.debuggable)
        
        assertEquals(1, result.activities.size)
        val activity = result.activities[0]
        assertEquals(".MainActivity", activity.name)
        assertTrue(activity.isExported)
        assertTrue(activity.isLauncher)
        
        assertEquals(activity, result.launcherActivity)
    }

    @Test
    fun `parse manifest with multiple components`() {
        val manifest = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.example.app">
                <application>
                    <activity android:name=".ActivityOne" />
                    <activity android:name=".ActivityTwo" />
                    <service android:name=".ServiceOne" android:process=":remote" />
                    <receiver android:name=".ReceiverOne" android:exported="true" />
                    <provider
                        android:name=".ProviderOne"
                        android:authorities="com.example.app.provider"
                        android:exported="false" />
                </application>
            </manifest>
        """.trimIndent()

        manifestFile = tempFolder.newFile("AndroidManifest.xml")
        manifestFile.writeText(manifest)

        val result = parser.parse(manifestFile)

        assertNotNull(result)
        assertEquals(2, result.activities.size)
        assertEquals(1, result.services.size)
        assertEquals(1, result.receivers.size)
        assertEquals(1, result.providers.size)

        val service = result.services[0]
        assertEquals(".ServiceOne", service.name)
        assertEquals(":remote", service.process)

        val receiver = result.receivers[0]
        assertEquals(".ReceiverOne", receiver.name)
        assertTrue(receiver.exported)

        val provider = result.providers[0]
        assertEquals(".ProviderOne", provider.name)
        assertFalse(provider.exported)
    }

    @Test
    fun `parse invalid manifest returns null`() {
        val manifest = """
            <?xml version="1.0" encoding="utf-8"?>
            <invalid>
                <xml>content</xml>
            </invalid>
        """.trimIndent()

        manifestFile = tempFolder.newFile("AndroidManifest.xml")
        manifestFile.writeText(manifest)

        val result = parser.parse(manifestFile)
        assertNull(result)
    }

    @Test
    fun `parse manifest without package returns null`() {
        val manifest = """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application>
                    <activity android:name=".MainActivity" />
                </application>
            </manifest>
        """.trimIndent()

        manifestFile = tempFolder.newFile("AndroidManifest.xml")
        manifestFile.writeText(manifest)

        val result = parser.parse(manifestFile)
        assertNull(result)
    }
}