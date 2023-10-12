package ch.kleis.lcaac.plugin.project

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral
import ch.kleis.lcaac.core.prelude.Prelude
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.xml.bind.DatatypeConverter
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists


typealias UnitBlock = CharSequence

class UnitLcaFileFromPreludeGenerator<Q> {

    private val existingRefUnit = mutableMapOf<Dimension, EUnitLiteral<Q>>()

    fun recreate(path: Path) {
        val newContent = getContent()
        val newHash = hash(newContent.toByteArray(StandardCharsets.UTF_8))
        if (haveToRecreate(path, newHash)) {
            if (path.exists()) path.deleteExisting()
            val file = path.toFile()
            file.createNewFile()
            FileOutputStream(file)
                .use { fileOut ->
                    ZipOutputStream(fileOut).use { jar ->
                        val je = ZipEntry("${Prelude.PKG_NAME}.lca")
                        je.comment = Prelude.PKG_NAME
                        jar.putNextEntry(je)
                        jar.write(newContent.toByteArray())
                        jar.closeEntry()
                        val jeMd5 = ZipEntry("${Prelude.PKG_NAME}.lca.md5")
                        jeMd5.comment = "${Prelude.PKG_NAME}_mda"
                        jar.putNextEntry(jeMd5)
                        jar.write(newHash.toByteArray())
                        jar.closeEntry()
                    }
                }
        }
    }

    private fun haveToRecreate(path: Path, newHash: String): Boolean {
        if (path.exists()) {
            val oldHash = readEntry(path, "${Prelude.PKG_NAME}.lca.md5")
            if (oldHash == newHash) {
                return false
            }
        }
        return true
    }

    private fun readEntry(path: Path, entryName: String): String? {
        FileInputStream(path.toFile()).use { fis ->
            BufferedInputStream(fis).use { bis ->
                ZipInputStream(bis).use { stream ->
                    var entry: ZipEntry?
                    while ((stream.nextEntry.also { entry = it } != null)) {
                        if (entry!!.name == entryName) {
                            InputStreamReader(stream).use { reader ->
                                return reader.readText()
                            }
                        }
                    }
                    return null
                }
            }
        }
    }

    private fun hash(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(bytes)
        val digest = md.digest()
        return DatatypeConverter.printHexBinary(digest).uppercase(Locale.getDefault())
    }

    private fun getContent(): String {
        val buffer = StringBuilder()
        buffer.append("package ${Prelude.PKG_NAME}\n")
        Prelude.primitiveUnits<Q>().values
            .filter { it.value.scale == 1.0 }
            .mapNotNull { mapUnitWithNewDimension(it.ref(), it.value) }
            .forEach { buffer.append(it.toString()) }
        Prelude.compositeUnits<Q>().values
            .map { mapUnitWithAlias(it.ref(), it.value, it.rawAlias) }
            .forEach { buffer.append(it.toString()) }
        return buffer.toString()
    }


    private fun mapUnitWithAlias(ref: String, unit: EUnitLiteral<Q>, alias: String): UnitBlock {
        return """
            
            unit $ref {
                symbol = "${unit.symbol}"
                alias_for = $alias
            }
        """.trimIndent()
    }

    private fun mapUnitWithNewDimension(ref: String, unit: EUnitLiteral<Q>): UnitBlock? {
        return if (existingRefUnit.containsKey(unit.dimension)) {
            null
        } else {
            existingRefUnit[unit.dimension] = unit
            """
                
                unit $ref {
                    symbol = "${unit.symbol}"
                    dimension = "${unit.dimension}"
                }
            """.trimIndent()
        }
    }

}
