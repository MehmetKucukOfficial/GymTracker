package com.gymtracker.data.photos

import androidx.annotation.XmlRes
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymtracker.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class PhotoBackupPolicyTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun managedPhotosAreExcludedFromLegacyCloudBackup() {
        val policy = readPolicy(R.xml.backup_rules)

        assertEquals(1, policy.excludedPhotoDirectories)
        assertTrue("Legacy full-backup root is missing", "full-backup-content" in policy.sections)
    }

    @Test
    fun managedPhotosAreExcludedFromCloudBackupAndDeviceTransfer() {
        val policy = readPolicy(R.xml.data_extraction_rules)

        assertEquals(2, policy.excludedPhotoDirectories)
        assertTrue("Cloud-backup exclusion is missing", "cloud-backup" in policy.sections)
        assertTrue("Device-transfer exclusion is missing", "device-transfer" in policy.sections)
    }

    private fun readPolicy(@XmlRes resourceId: Int): ParsedPolicy {
        val parser = context.resources.getXml(resourceId)
        val sections = mutableSetOf<String>()
        var exclusions = 0
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                sections += parser.name
                if (
                    parser.name == "exclude" &&
                    parser.getAttributeValue(null, "domain") == "file" &&
                    parser.getAttributeValue(null, "path") == "progress_photos/"
                ) {
                    exclusions++
                }
            }
            parser.next()
        }
        parser.close()
        return ParsedPolicy(sections, exclusions)
    }

    private data class ParsedPolicy(
        val sections: Set<String>,
        val excludedPhotoDirectories: Int
    )
}
