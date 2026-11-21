package dev.nitpick.app

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

/** Asks the user for a folder and returns its path, or null if they cancel. */
fun pickDirectory(parent: Frame): String? =
    if (System.getProperty("os.name").startsWith("Mac")) {
        pickWithNativeMacDialog(parent)
    } else {
        pickWithFileChooser(parent)
    }

// AWT's FileDialog is the native macOS dialog, and this property makes it pick folders.
private fun pickWithNativeMacDialog(parent: Frame): String? {
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    try {
        val dialog = FileDialog(parent)
        dialog.isVisible = true
        val directory = dialog.directory ?: return null
        val name = dialog.file ?: return null
        return File(directory, name).absolutePath
    } finally {
        System.setProperty("apple.awt.fileDialogForDirectories", "false")
    }
}

// On Windows and Linux, FileDialog cannot select folders; Swing's chooser can.
private fun pickWithFileChooser(parent: Frame): String? {
    val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
    return if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.absolutePath
    } else {
        null
    }
}
