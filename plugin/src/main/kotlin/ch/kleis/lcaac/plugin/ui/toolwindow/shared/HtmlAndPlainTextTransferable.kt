package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.Reader
import java.io.StringReader

/**
 * Transferable offering the same text as HTML and as plain text, as String or Reader.
 *
 * Replaces org.jdesktop.swingx.plaf.basic.core.BasicTransferable, which is no longer bundled with the IDE.
 */
class HtmlAndPlainTextTransferable(plain: String, html: String) : Transferable {
    private val dataByFlavor: Map<DataFlavor, String> =
        listOf("text/html" to html, "text/plain" to plain)
            .flatMap { (mimeType, data) ->
                listOf(String::class.java, Reader::class.java)
                    .map { DataFlavor("$mimeType;class=${it.name}") to data }
            }
            .plus(DataFlavor.stringFlavor to plain)
            .toMap()

    override fun getTransferDataFlavors(): Array<DataFlavor> = dataByFlavor.keys.toTypedArray()

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = dataByFlavor.containsKey(flavor)

    override fun getTransferData(flavor: DataFlavor?): Any {
        val data = dataByFlavor[flavor] ?: throw UnsupportedFlavorException(flavor)
        return if (flavor!!.representationClass == Reader::class.java) StringReader(data) else data
    }
}
