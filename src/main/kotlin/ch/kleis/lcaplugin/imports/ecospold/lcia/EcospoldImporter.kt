package ch.kleis.lcaplugin.imports.ecospold.lcia

import ch.kleis.lcaplugin.core.lang.evaluator.toUnitValue
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.ide.imports.ecospold.EcospoldImportSettings
import ch.kleis.lcaplugin.imports.*
import ch.kleis.lcaplugin.imports.ecospold.lcia.model.ActivityDataset
import ch.kleis.lcaplugin.imports.model.ImportedUnit
import ch.kleis.lcaplugin.imports.shared.UnitRenderer
import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class EcospoldImporter(private val settings: EcospoldImportSettings, private val methodName: String) : Importer() {
    companion object {
        private val LOG = Logger.getInstance(EcospoldImporter::class.java)

        fun unitToStr(u: String): String {
            return if (u != "metric ton*km") u else "ton*km"
        }

        fun getMethodNames(libFile: String): List<String> {
            val path = Path.of(libFile)
            try {
                SevenZFile(path.toFile()).use { f ->
                    val methodsFile = f.entries.firstOrNull { it.name.endsWith("ImpactMethods.xml") }
                    f.getInputStream(methodsFile).use {
                        return Parser.readMethodName(it)
                    }
                }
            } catch (e: Exception) {
                return listOf("")
            }
        }
    }

    private var totalValue = 1
    private var currentValue = 0
    private val processRenderer = Ecospold2ProcessRenderer()
    private val predefinedUnits = Prelude.unitMap.values
        .map { it.toUnitValue() }
        .associateBy { it.symbol.toString() }
    private val unitRenderer = UnitRenderer.of(predefinedUnits)

    override fun importAll(controller: AsyncTaskController, watcher: AsynchronousWatcher) {
        val path = Path.of(settings.libraryFile)

        val pkg = settings.rootPackage.ifBlank { "default" }
        SevenZFile(path.toFile()).use { f ->
            ModelWriter(pkg, settings.rootFolder, listOf(), watcher).use { w ->
                importEntries(f, w, controller, watcher)
            }
        }
    }

    override fun getImportRoot(): Path {
        return Path.of(settings.rootFolder)
    }

    override fun collectProgress(): List<Imported> {
        return listOf(
            Imported(unitRenderer.nbUnit, "units"),
            Imported(processRenderer.nbProcesses, "processes"),
            Imported(processRenderer.nbProcesses, "substances"),
        )
    }

    private fun importEntries(
        f: SevenZFile,
        writer: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
        val start = Instant.now()
        val entries = f.entries.toList()
        totalValue = entries.size

        processRenderer.processDict = readProcessDict(f, entries)

        if (settings.importUnits) {
            val unitConversionFile = entries.firstOrNull { it.name.endsWith("UnitConversions.xml") }
            val fromMeta = f.getInputStream(unitConversionFile).use {
                val unitConvs = Parser.readUnits(it)
                unitConvs.asSequence()
                    .map { u ->
                        ImportedUnit(
                            u.dimension, u.fromUnit, u.factor,
                            unitToStr(u.toUnit)
                        )
                    }
                    .filter { u -> u.name != "foot-candle" }
            }
            val methodsFile = entries.firstOrNull { it.name.endsWith("ImpactMethods.xml") }
            val fromMethod = f.getInputStream(methodsFile).use {
                val unitConvs = Parser.readMethodUnits(it, methodName)
                unitConvs.asSequence()
                    .map { u ->
                        ImportedUnit(
                            u.dimension, u.fromUnit, u.factor,
                            unitToStr(u.toUnit)
                        )
                    }
                    .filter { u -> u.name != "foot-candle" }
                    .filter { u -> !predefinedUnits.containsKey(u.name) }
            }


            (fromMeta + fromMethod)
                .distinctBy { it.name }
                .forEach { unitRenderer.render(it, writer) }
        }
        entries.asSequence()
            .filter { it.hasStream() }
            .filter { it.name.endsWith(".spold") }
            .forEach {
                importEntry(it.name, f.getInputStream(it), writer, controller, watcher)
            }
        val duration = Duration.between(start, Instant.now())
        renderMain(writer, unitRenderer.nbUnit, processRenderer.nbProcesses, methodName, duration)
    }

    private fun renderMain(writer: ModelWriter, nbUnits: Int, nbProcess: Int, methodName: String, duration: Duration) {
        val s = duration.seconds
        val durAsStr = String.format("%02dm %02ds", s / 60, (s % 60));
        val block = """
            Import Method: $methodName
            Date: ${ZonedDateTime.now().toString()}
            Import Summary:
                * $nbUnits units
                * $nbProcess processes
                * $nbProcess substances
            Duration: $durAsStr
        """.trimIndent()

        writer.write("main", ModelWriter.pad(ModelWriter.asComment(block), 0), false)
    }

    data class ProcessDictRecord(
        val processId: String,
        val fileName: String,
        val processName: String,
        val geo: String,
        val productName: String
    )

    private fun readProcessDict(f: SevenZFile, entries: List<SevenZArchiveEntry>): Map<String, ProcessDictRecord> {
        val dictEntry = entries.first { it.name.endsWith("FilenameToActivityLookup.csv") }
        val csvFormat = CSVFormat.Builder.create().setDelimiter(";").setHeader().build()
        val records = CSVParser.parse(f.getInputStream(dictEntry), Charset.defaultCharset(), csvFormat)
        return records.map {
            ProcessDictRecord(
                it["Filename"].substring(0, it["Filename"].indexOf("_")),
                it["Filename"],
                it["ActivityName"],
                it["Location"],
                it["ReferenceProduct"],
            )
        }.associateBy { it.processId }
    }

    private fun importEntry(
        path: String,
        input: InputStream,
        w: ModelWriter,
        controller: AsyncTaskController,
        watcher: AsynchronousWatcher
    ) {
        if (!controller.isActive()) throw ImportInterruptedException()
        val eco = Parser.readDataset(input)
        currentValue++
        watcher.notifyProgress((100.0 * currentValue / totalValue).roundToInt())
        importDataSet(eco.activityDataset, w, path)
    }

    private fun importDataSet(
        dataSet: ActivityDataset,
        w: ModelWriter,
        path: String
    ) {
        LOG.info("Read dataset from $path")
        processRenderer.render(dataSet, w, "from $path", methodName)

    }


}