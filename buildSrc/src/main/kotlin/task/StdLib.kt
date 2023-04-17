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
        .replace(nonAlphaNumeric, "_")
        .trimEnd('_')
}

fun generateZipEntry(outputStream: ZipOutputStream, currentFileName: String, zipEntryContent: String) {
    val parameters = ZipParameters()
    parameters.fileNameInZip = "$currentFileName.lca"
    outputStream.putNextEntry(parameters)
    outputStream.write(zipEntryContent.encodeToByteArray())
    outputStream.closeEntry()
}
