package task

import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters


fun sanitizeString(s: String): String {
    if (s.isBlank()) {
        return s
    }
    val r = if (s[0].isDigit()) "_$s" else s
    val spaces = """\s+""".toRegex()
    val nonAlphaNumeric = """[^a-zA-Z0-9]+""".toRegex()
    return r.replace(spaces, "_")
        .replace(">", "_gt_")
        .replace("<", "_lt_")
        .replace("/", "_sl_")
        .replace(nonAlphaNumeric, "_")
        .trimEnd('_')
}

fun generateZipEntry(outputStream: ZipOutputStream, currentFileName: String, zipEntryContent: String) {
    val parameters = ZipParameters()
    if (currentFileName.contains("/")) {
        val index = currentFileName.indexOf("/")
        val folderParam = ZipParameters()
        folderParam.fileNameInZip = currentFileName.substring(0, index)
        outputStream.putNextEntry(folderParam)
        parameters.fileNameInZip = currentFileName
    } else {
        parameters.fileNameInZip = "$currentFileName.lca"
    }
    outputStream.putNextEntry(parameters)
    outputStream.write(zipEntryContent.encodeToByteArray())
    outputStream.closeEntry()
}
