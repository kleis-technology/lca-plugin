package task

import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters


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
