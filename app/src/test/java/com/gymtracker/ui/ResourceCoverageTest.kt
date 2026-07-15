package com.gymtracker.ui

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ResourceCoverageTest {
    @Test
    fun englishDefinesEveryDefaultStringAndPlural() {
        val defaultFile = resourceFile("src/main/res/values/strings.xml")
        val englishFile = resourceFile("src/main/res/values-en/strings.xml")

        assertEquals(resourceKeys(defaultFile), resourceKeys(englishFile))
        assertFalse(defaultFile.readText().contains("MissingTranslation"))
    }

    private fun resourceKeys(file: File): Set<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val children = document.documentElement.childNodes
        return buildSet {
            for (index in 0 until children.length) {
                val node = children.item(index)
                if (node.nodeName == "string" || node.nodeName == "plurals") {
                    add("${node.nodeName}:${node.attributes.getNamedItem("name").nodeValue}")
                }
            }
        }
    }

    private fun resourceFile(path: String): File = sequenceOf(File(path), File("app/$path"))
        .firstOrNull(File::isFile)
        ?: error("Resource file not found: $path")
}
