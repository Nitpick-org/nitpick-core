package dev.nitpick.app.ui

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Reads the EN and FR resource files straight from disk: the runtime lookup needs a display on
 * desktop, and the only thing at stake is that each count has a singular and a plural form.
 */
class CountStringsTest {

    private val expected = mapOf(
        "status_commits" to mapOf("values" to ("%1\$d commit" to "%1\$d commits"), "values-fr" to ("%1\$d commit" to "%1\$d commits")),
        "commit_bar_applied" to mapOf("values" to ("%1\$d suggestion applied" to "%1\$d suggestions applied"), "values-fr" to ("%1\$d suggestion appliquée" to "%1\$d suggestions appliquées")),
        "review_nits" to mapOf("values" to ("%1\$d nit" to "%1\$d nits"), "values-fr" to ("%1\$d nit" to "%1\$d nits")),
        "diff_files" to mapOf("values" to ("%1\$d file" to "%1\$d files"), "values-fr" to ("%1\$d fichier" to "%1\$d fichiers")),
        "review_changed_files" to mapOf("values" to ("on %1\$d changed file" to "on %1\$d changed files"), "values-fr" to ("sur %1\$d fichier modifié" to "sur %1\$d fichiers modifiés")),
    )

    @Test
    fun everyCountHasASingularAndAPluralInBothLanguages() {
        for ((name, perFolder) in expected) {
            for ((folder, forms) in perFolder) {
                assertEquals(forms, pluralForms(folder, name), "$folder/$name")
            }
        }
    }

    private fun pluralForms(folder: String, name: String): Pair<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/commonMain/composeResources/$folder/strings.xml"))
        val plurals = document.getElementsByTagName("plurals")
        val node = (0 until plurals.length).map { plurals.item(it) }.first { it.attributes.getNamedItem("name").nodeValue == name }
        val items = (0 until node.childNodes.length).map { node.childNodes.item(it) }.filter { it.nodeName == "item" }
        val byQuantity = items.associate { it.attributes.getNamedItem("quantity").nodeValue to it.textContent }
        return byQuantity.getValue("one") to byQuantity.getValue("other")
    }
}
