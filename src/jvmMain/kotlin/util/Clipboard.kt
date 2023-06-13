package util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun copyToClipboard(string: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(string), null)
}